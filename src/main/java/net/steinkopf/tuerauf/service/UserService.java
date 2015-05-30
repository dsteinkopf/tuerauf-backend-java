package net.steinkopf.tuerauf.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
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

    final static int MAX_SERIAL_ID = 16;


    /**
     * Activates all users which are not active and new.
     */
    public void activateAllNew() {

        List<User> userList = userRepository.findByActiveFalseAndNewUserTrue();
        userList.forEach(user -> { user.setActive(true); userRepository.save(user); });
    }

    public User registerOrUpdateUser(final String username, final String pin, final String installationId) throws Exception {

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
        }
        user.updateData(username, pin);
        userRepository.save(user);

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
        // TODO Montoring-Mail
        throw new IndexOutOfBoundsException("too many users - MAX_SERIAL_ID reached");
    }

}
