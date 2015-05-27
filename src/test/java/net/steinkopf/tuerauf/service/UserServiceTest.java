package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.SecurityContextTest;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link UserService}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
public class UserServiceTest extends SecurityContextTest {

    @Autowired
    UserRepository userRepository;

    /**
     * System under Test.
     */
    @Autowired
    UserService userService;


    @Before
    public void setup() {

        super.setup();

        User user1 = new User("testusername");
        userRepository.save(user1);
    }

    @Test
    public void testActivateAllNew() throws Exception {

        // Prepare
        assertThat(userRepository.findByActive(false).size(), is(equalTo(1)));

        // Run
        userService.activateAllNew();

        // Check
        assertThat(userRepository.findByActive(false).size(), is(equalTo(0)));
    }
}
