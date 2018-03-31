package net.steinkopf.tuerauf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import net.steinkopf.tuerauf.SecurityContextTest;
import net.steinkopf.tuerauf.TestConstants;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.data.AccessLog;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.AccessLogRepository;
import net.steinkopf.tuerauf.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for {@link UserService}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
public class UserServiceTest extends SecurityContextTest {

    private final static Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private AccessLogService accessLogService;

    /**
     * System under Test.
     */
    @Autowired
    private UserService userService;

    @Before
    public void setup() throws Exception {

        super.setup();
    }

    @After
    public void tearDown() throws Exception {

        // delete users created by any test.
        // those from import.sql must not be deleted.
        Stream.of(0L, 4L, 5L, 6L)
            .map(id -> userRepository.findOne(id))
            .filter(Objects::nonNull)
            .forEach(user -> userRepository.delete(user));
    }

    @Test
    public void testActivateAllNew() throws Exception {

        // Prepare
        int inActiveBefore = userRepository.findByActive(false).size();
        int activeBefore = userRepository.findByActive(true).size();

        User user1 = new User("testInstallationId", "UserServiceTest user");
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
        int serialIdToBeReused = createThenDeleteSomeUsers("serId ", userList, USER_TO_DELETE);

        // re-create
        User user = new User("testInstId99", "user 99");
        int serialId = userService.findFreeSerialId();
        user.setSerialId(serialId);
        userRepository.save(user);

        // check
        assertThat(serialId, is(equalTo(serialIdToBeReused)));
    }

    /**
     * create, then delete some users
     *
     * @return serialId to be reused
     */
    private int createThenDeleteSomeUsers(final String prefix, final List<User> userList, final int userToDelete) {

        // first create some Users, then delete one. This serialId should be reused then.
        for (int userNumber = 0; userNumber < 4; userNumber++) {
            User user = new User(prefix + "InstId" + userNumber, prefix + "user " + userNumber);
            int serialId = userService.findFreeSerialId();
            user.setSerialId(serialId);
            userRepository.save(user);
            logger.debug("created {}", user);

            userList.add(userNumber, user);
        }

        // remember serialId and delete
        int serialIdToBeReused = userList.get(userToDelete).getSerialId();
        userRepository.delete(userList.get(userToDelete));
        return serialIdToBeReused;
    }

    @Test(expected = IndexOutOfBoundsException.class)
    @Transactional // makes insertions be rolled back on exception.
    public void testFindFreeSerialIdOverflow() throws Exception {

        userRepository.findAll().forEach(user1 -> logger.debug("all users: {}", user1.toString()));

        // create more users than available serialIds
        for (int userNumber = 0; userNumber < UserService.MAX_SERIAL_ID + 1; userNumber++) {
            User user = new User("ovfl InstId" + userNumber, "ovfl user " + userNumber);
            int serialId = userService.findFreeSerialId();
            user.setSerialId(serialId);
            logger.debug("save user with serialId {}", serialId);
            userRepository.save(user);
        }
    }

    @Test
    public void testGetPinList() throws Exception {

        // Prepare
        List<User> userList = new ArrayList<>();
        final int USER_TO_DELETE = 2;
        createThenDeleteSomeUsers("pin ", userList, USER_TO_DELETE);

        // Run
        final String[] pinList = userService.getActivePinList();

        // Check
        for (int serialId = 0; serialId < UserService.MAX_SERIAL_ID; serialId++) {
            final String pin = pinList[serialId];
            switch (serialId) {
                case TestConstants.SERIAL_ID_ALMOST_ACTIVE:
                    assertThat(pin, is(equalTo(TestConstants.PIN_ALMOST_ACTIVE)));
                    break;
                case TestConstants.SERIAL_ID_INACTIVE:
                    assertThat(pin, is(equalTo(null))); // inactive user's pin not sent.
                    break;
                default:
                    assertThat(pin, is(equalTo(null)));
                    break;
            }
        }
    }

    @Test
    public void testRegisterOrUpdateUser() throws Exception {

        User user1 = userService.registerOrUpdateUser("User1", "1111", "InstIdUser1");
        User user1b = userService.registerOrUpdateUser("User1b", "1112", "InstIdUser1");
        assertThat(user1b.getId(), is(equalTo(user1.getId())));

        //noinspection OptionalGetWithoutIsPresent
        User user1read = userRepository.findByInstallationId("InstIdUser1").get();
        assertThat(user1read.getId(), is(equalTo(user1.getId())));
        assertThat(user1read.getUsername(), is(equalTo("User1b")));
        assertThat(user1read.getUsernameOld(), is(equalTo("User1")));
    }

    @Test(expected = UserService.DuplicateUsernameException.class)
    public void testRegisterOrUpdateUserExists() throws Exception {

        //noinspection unused
        User user1 = userService.registerOrUpdateUser("User1", "1111", "InstIdUser1");
        //noinspection unused
        User user2 = userService.registerOrUpdateUser("User1", "1112", "InstIdUser2");
    }

    @Test
    @Transactional // makes insertions be rolled back on exception.
    public void testJoinNewUserToExistingUser() throws Exception {

        // prepare:
        User newUser = userService.registerOrUpdateUser("NewUserToJoin1", "2387", "NewJInstId1");
        User existingUser = userRepository.findOne(3L); // active user
        final long newUserId = newUser.getId();

        // do it:
        userService.joinNewUserToExistingUser(newUser.getId(), existingUser.getId());

        // check:

        assertThat(userRepository.countById(newUser.getId()), is(equalTo(0L))); // new user removed
        assertThat(userRepository.countById(existingUser.getId()), is(equalTo(1L))); // existing user still there

        Optional<User> checkNewUser1 = userRepository.findById(newUser.getId());
        assertFalse(String.format("new user id %d must not exist anymore now", newUser.getId()),
            checkNewUser1.isPresent());

        // new installation, username id must be copied
        Optional<User> checkNewUser2 = userRepository.findByInstallationId("NewJInstId1");
        assertTrue(checkNewUser2.isPresent());
        assertThat(checkNewUser2.get().getId(), is(equalTo(3L)));
        Optional<User> checkNewUser3 = userRepository.findByUsername("NewUserToJoin1");
        assertThat(checkNewUser3.isPresent(), is(true));
        //noinspection ConstantConditions
        assertThat(checkNewUser3.get().getId(), is(equalTo(3L)));

        User joinedUser = userRepository.findOne(3L); // save active user id
        assertThat(joinedUser.getInstallationId(), is(equalTo("NewJInstId1")));
        assertThat(joinedUser.getPin(), is(equalTo("2387")));
        assertThat(joinedUser.getUsername(), is(equalTo("NewUserToJoin1")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJoinNewUserToExistingUser_Fail_EqualUsers() throws Exception {
        userService.joinNewUserToExistingUser(2L, 2L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJoinNewUserToExistingUser_Fail_MissingExisting() throws Exception {
        userService.joinNewUserToExistingUser(2L, 99L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJoinNewUserToExistingUser_Fail_MissingNew() throws Exception {
        userService.joinNewUserToExistingUser(99L, 2L);
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional // makes insertions be rolled back on exception.
    public void testJoinNewUserToExistingUser_fail_AccessLog() throws Exception {

        // prepare:
        User newUser = userService.registerOrUpdateUser("NewUserToJoin1", "2387", "NewJInstId1");
        User existingUser = userRepository.findOne(3L); // active user
        accessLogService.log(newUser, AccessLog.AccessType.checkLocation, 1.0, 1.2, "bla");

        // do it:
        userService.joinNewUserToExistingUser(newUser.getId(), existingUser.getId());
    }

    @Test
    @Transactional // makes insertions be rolled back on exception.
    public void testJoinNewUserToExistingUser_AccessLog_untouched() throws Exception {

        // prepare:
        User newUser = userService.registerOrUpdateUser("NewUserToJoin1", "2387", "NewJInstId1");
        User existingUser = userRepository.findOne(3L); // active user
        accessLogService.log(existingUser, AccessLog.AccessType.checkLocation, 1.0, 1.2, "bla");
        accessLogService.log(existingUser, AccessLog.AccessType.checkLocation, 1.0, 1.2, "bla2");
        assertThat(accessLogRepository.countByUser(existingUser), is(2L));

        // do it:
        userService.joinNewUserToExistingUser(newUser.getId(), existingUser.getId());

        // check
        assertThat(accessLogRepository.countByUser(existingUser), is(2L));
        accessLogService.log(existingUser, AccessLog.AccessType.checkLocation, 1.0, 1.2, "bla3");
        assertThat(accessLogRepository.countByUser(existingUser), is(3L));
    }
}
