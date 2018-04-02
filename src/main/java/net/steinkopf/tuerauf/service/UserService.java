package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.AccessLogRepository;
import net.steinkopf.tuerauf.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Manipulation of users.
 */
@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private LogAndMailService logAndMailService;


    /**
     * Must be set to same same value as in arduino code.
     */
    static final int MAX_SERIAL_ID = 16;


    /**
     * Activates all users which are not active and new.
     *
     * @return List of users that have been activated.
     */
    public List<User> activateAllNew() {

        logger.trace("activateAllNew");
        List<User> userList = userRepository.findByActiveFalseAndNewUserTrue();
        userList.forEach(user -> {
            user.setActive(true);
            userRepository.save(user);
        });
        return userList;
    }

    /**
     * Creates a user object or - if installationId exists - updates this user.
     *
     * @return created or updated user object.
     * @throws DuplicateUsernameException if this username already exists.
     */
    public User registerOrUpdateUser(final String username, final String pin, final String installationId)
            throws DuplicateUsernameException {

        Assert.isTrue(username.length() >= 2, "username must be at least 2 characters");
        Assert.isTrue(pin.length() == 4, "pin must be exactly 4 characters");
        Assert.isTrue(installationId.length() >= 10, "installationId must be at least 10 characters");

        Optional<User> existingUser = userRepository.findByInstallationId(installationId);
        User user;
        if ( ! existingUser.isPresent()) {
            existingUser = userRepository.findByUsername(username);  // can only be 1 or none.
            if ( ! existingUser.isPresent()) {
                // create new User
                user = new User(installationId, username);
                user.setSerialId(findFreeSerialId());
            }
            else {
                throw new DuplicateUsernameException("Username " + username + " already exists.");
            }
        } else {
            // update existing User
            user = existingUser.get();
            user.setNewUser(false);
        }
        user.updateData(username, pin);
        userRepository.save(user);

        // Log and send admin notifications:
        if (user.getUsernameOld() != null) {
            logAndMailService.logAndMail("user {} changed name to {} (serialId={})",
                    null,
                    user.getUsernameOld(),
                    user.getUsername(),
                    user.getSerialId()
            );
        }
        if (user.getPinOld() != null) {
            logAndMailService.logAndMail("user {} changed pin (serialId={})",
                    null,
                    user.getUsername(),
                    user.getSerialId()
            );
        }
        logAndMailService.logAndMail("user {} {} (serialId={})",
                null,
                user.getUsername(),
                existingUser.isPresent() ? "updated" : "created",
                user.getSerialId()
        );

        return user;
    }

    /**
     * Checks if this user is active
     *
     * @return user object, if existing and active, empty if not.
     */
    public Optional<User> getUserIfActive(final String installationId) {

        final Optional<User> userOptional = userRepository.findByInstallationId(installationId);
        if ( ! userOptional.isPresent()) {
            return Optional.empty();
        }
        if ( ! userOptional.get().isActive()) {
            return Optional.empty();
        }
        return userOptional;
    }

    /**
     * Find the lowest free (usable) serial id
     *
     * @return free serial id.
     * @throws IndexOutOfBoundsException if all serial ids are used.
     */
    @SuppressWarnings("WeakerAccess")
    public int findFreeSerialId() {

        boolean[] serialIdIsUsed = new boolean[MAX_SERIAL_ID];

        // mark used serialIds
        for (User user : userRepository.findAll()) {
            if (user.hasSerialId()) {
                serialIdIsUsed[user.getSerialId()] = true;
            }
        }

        // find first free serialId
        for (int serialId = 0; serialId < MAX_SERIAL_ID; serialId++) {
            if (!serialIdIsUsed[serialId]) {
                logger.debug("findFreeSerialId returns serialId {}", serialId);
                return serialId;
            }
        }

        // none found
        logAndMailService.logAndMail("too many users - MAX_SERIAL_ID reached", null);
        throw new IndexOutOfBoundsException("too many users - MAX_SERIAL_ID reached");
    }

    /**
     * Create an ArrayList of the pins of active users.
     * Note: After sending pins to Arduino, they are deleted in the local DB. So here we return only pins net yet deleted.
     *
     * @return Array of pins. Index of Array = serialId.
     */
    public String[] getActivePinList() {

        String[] pins = new String[MAX_SERIAL_ID];
        for (final User user : userRepository.findAll()) {
            if (user.getPin() != null && user.isActive() && user.hasSerialId()) {
                pins[user.getSerialId()] = user.getPin();
            }
        }
        return pins;
    }

    /**
     * Locally delete given pins.
     *
     * @param pinList Pins to be deleted.
     * @exception IllegalArgumentException if any user does not exist.
     */
    public void deletePins(final String[] pinList) {

        IntStream.range(0, pinList.length)
                .filter(serialId -> pinList[serialId] != null)
                .forEach(serialId -> {
                    final User user = userRepository
                            .findBySerialId(serialId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    String.format("User with serialId %s does not exist", serialId)));
                    user.setPin(null);
                    userRepository.save(user);
                });
    }

    /**
     * @return The number of currently existing users.
     */
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * Remove the new User and give the existing user the new user's installationId, pin and username.
     *
     * @param newUserId User that has just been created - must not have any access logs.
     * @param existingUserId Old/existing user.
     * @exception IllegalArgumentException if any user does not exist
     *                                      or both are equal.
     *                                      or new user has access logs.
     */
    public void joinNewUserToExistingUser(final long newUserId, final long existingUserId) {
        final User newUser = userRepository
            .findById(newUserId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("New user with id %s does not exist.", newUserId)));
        final User existingUser = userRepository
            .findById(existingUserId)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Existing user with id %s does not exist.", existingUserId)));

        // check if join is possible
        if (newUser.equals(existingUser)) {
            throw new IllegalArgumentException(
                String.format("New user %s and existing user %s must be different.",
                    newUser.getUsername(), existingUser.getUsername()));
        }
        if (accessLogRepository.countByUser(newUser) != 0) {
            throw new IllegalArgumentException(
                String.format("New user %s must not have any access log.", newUser));
        }

        // remove new user:
        userRepository.delete(newUser);

        // now join:
        existingUser.setPinOld(existingUser.getPin());
        existingUser.setPin(newUser.getPin());
        existingUser.setUsernameOld(existingUser.getUsername());
        existingUser.setUsername(newUser.getUsername());
        existingUser.setInstallationId(newUser.getInstallationId());
        // serial id is kept in existing user.
        // activation state is unchanged.
        userRepository.save(existingUser);

        logAndMailService.logAndMail("User {} joined to {}.",
            null,
            newUser,
            existingUser
        );
    }

    public class DuplicateUsernameException extends Exception {

        DuplicateUsernameException(final String message) {
            super(message);
        }
    }
}
