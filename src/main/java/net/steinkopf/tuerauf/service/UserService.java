package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manipulation of users.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;


    /**
     * Activates all users which are not active and new.
     */
    public void activateAllNew() {

        List<User> userList = userRepository.findByActiveFalseAndNewUserTrue();
        userList.forEach(user -> { user.setActive(true); userRepository.save(user); });
    }

    public String registerUser(final String username, final String pin, final String installationId) {

        // TODO
        return "All OK. username=" + username;
    }
}
