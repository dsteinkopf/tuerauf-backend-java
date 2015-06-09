package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Manipulation of users.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogAndMailService logAndMailService;


    final static int MAX_SERIAL_ID = 16;


    /**
     * Activates all users which are not active and new.
     */
    public void activateAllNew() {

        logger.trace("activateAllNew");
        List<User> userList = userRepository.findByActiveFalseAndNewUserTrue();
        userList.forEach(user -> { user.setActive(true); userRepository.save(user); });
    }

    /**
     * Creates a user object or - if installationId exists - updates this user.
     * @return created or updated user object.
     */
    public User registerOrUpdateUser(final String username, final String pin, final String installationId)  {

        Assert.isTrue(username.length() >= 2, "username must be at least 2 characters");
        Assert.isTrue(pin.length() == 4, "pin must be exactly 4 characters");
        Assert.isTrue(installationId.length() >= 10, "installationId must be at least 10 characters");

        List<User> existingUser = userRepository.findByInstallationId(installationId); // can only be 1 or none.
        User user;
        if (existingUser.isEmpty()) {
            // create new User
            user = new User(installationId);
            user.setSerialId(findFreeSerialId());
        }
        else {
            // update existing User
            user = existingUser.get(0);
            user.setNewUser(false);
        }
        user.updateData(username, pin);
        userRepository.save(user);

        // Log and send admin notifications:
        if (user.getUsernameOld() != null) {
            logAndMailService.logAndMail("user {} changed name to {} (installationId={})",
                    user.getUsernameOld(),
                    user.getUsername(),
                    user.getInstallationId()
            );
        }
        if (user.getPinOld() != null) {
            logAndMailService.logAndMail("user {} changed pin to {} (installationId={})",
                    user.getUsername(),
                    user.getPin(),
                    user.getInstallationId()
            );
        }
        logAndMailService.logAndMail("user {} saved (installationId={})",
                user.getUsername(),
                user.getInstallationId()
        );

        return user;
    }

    /**
     * Checks if this user is active
     * @return user object, if existing and active, null if not.
     */
    public User getUserIfActive(final String installationId) {

        final List<User> userList = userRepository.findByInstallationId(installationId);
        if (userList.size() == 0) {
            return null;
        }
        final User user = userList.get(0);
        if ( ! user.isActive()) {
            return null;
        }
        return user;
    }

    /**
     * Find the lowest free (usable) serial id
     * @return free serial id.
     * @throws Exception if all serial ids are used.
     */
    public int findFreeSerialId() throws IndexOutOfBoundsException {

        boolean serialIdIsUsed[] = new boolean[MAX_SERIAL_ID];
        userRepository.findAll().forEach(user -> {
            serialIdIsUsed[user.getSerialId()] = true;
        });
        for (int serialId = 0; serialId < MAX_SERIAL_ID; serialId++) {
            if ( ! serialIdIsUsed[serialId]) {
                logger.debug("findFreeSerialId returns serialId {}", serialId);
                return serialId;
            }
        }
        // TODO Global Exception Handler
        logAndMailService.logAndMail("too many users - MAX_SERIAL_ID reached");
        throw new IndexOutOfBoundsException("too many users - MAX_SERIAL_ID reached");
    }

}
