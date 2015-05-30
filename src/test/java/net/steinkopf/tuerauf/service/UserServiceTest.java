package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.SecurityContextTest;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link UserService}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
public class UserServiceTest extends SecurityContextTest {

    private final static Logger logger = LoggerFactory.getLogger(UserServiceTest.class);


    @Autowired
    UserRepository userRepository;

    /**
     * System under Test.
     */
    @Autowired
    UserService userService;


    @Before
    public void setup() throws Exception {

        super.setup();
    }

    @Test
    public void testActivateAllNew() throws Exception {

        // Prepare
        int inActiveBefore = userRepository.findByActive(false).size();
        int activeBefore = userRepository.findByActive(true).size();

        User user1 = new User("testInstallationId");
        user1.setUsername("UserServiceTest user");
        int serialId = userService.findFreeSerialId();
        user1.setSerialId(serialId);
        userRepository.save(user1);

        assertThat(userRepository.findByActive(false).size(), is(equalTo(inActiveBefore + 1)));
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore)));

        // Run
        userService.activateAllNew();

        // Check
        assertThat(userRepository.findByActive(false).size(), is(equalTo(0)));
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore + inActiveBefore + 1)));
    }

    @Test
    public void testFindFreeSerialId() throws Exception {

        List<User> userList = new ArrayList<>();

        final int USER_TO_DELETE = 1;

        // first create some Users, then delete one. This serialId should be reused then.
        for (int userNumber = 0; userNumber < 4; userNumber++) {
            User user = new User("testInstId" + userNumber);
            user.setUsername("user " + userNumber);
            int serialId = userService.findFreeSerialId();
            user.setSerialId(serialId);
            userRepository.save(user);
            logger.debug("created {}", user);

            userList.add(userNumber, user);
        }

        // remember serialId and delete
        int serialIdToBeReused = userList.get(USER_TO_DELETE).getSerialId();
        userRepository.delete(userList.get(USER_TO_DELETE));

        // re-create
        User user = new User("testInstId99");
        user.setUsername("user 99");
        int serialId = userService.findFreeSerialId();
        user.setSerialId(serialId);
        userRepository.save(user);

        // check
        assertThat(serialId, is(equalTo(serialIdToBeReused)));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    @Transactional // makes insertions be rolled back on exception.
    public void testFindFreeSerialIdOverflow() throws Exception {

        // create more users than available serialIds
        for (int userNumber = 0; userNumber < UserService.MAX_SERIAL_ID + 1; userNumber++) {
            User user = new User("testInstId" + userNumber);
            user.setUsername("user " + userNumber);
            int serialId = userService.findFreeSerialId();
            user.setSerialId(serialId);
            userRepository.save(user);
        }
    }
}
