package net.steinkopf.tuerauf.rest;

import net.steinkopf.tuerauf.SecurityContextTest;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link FrontendAPIRestController}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@WebAppConfiguration
public class FrontendAPIRestControllerTest extends SecurityContextTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mvc;

    private final static String REGISTER_USER_URL = FrontendAPIRestController.FRONTEND_URL + "/registerUser";

    private final static String TEST_INSTALLATION_ID = "1234567890";
    private final static String TEST_INSTALLATION_ID2 = "1234567891";
    private final static String TEST_PIN = "1111";
    private final static String TEST_PIN2 = "2222";
    private final static String TEST_USERNAME = "New Username";
    private final static String TEST_USERNAME2 = "Updated Username";
    private final static int TEST_SERIAL_ID = 0;
    private final static int TEST_SERIAL_ID2 = 1;


    @Before
    public void setUp() throws Exception {

        this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    public void testRegisterUser() throws Exception {

        // Prepare
        int inActiveBefore = userRepository.findByActive(false).size();
        int activeBefore = userRepository.findByActive(true).size();

        // Run
        this.mvc.perform(get(REGISTER_USER_URL)
                        .param("username", TEST_USERNAME)
                        .param("pin", TEST_PIN)
                        .param("installationId", TEST_INSTALLATION_ID)
                        .param("appsecret", "secretApp")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("saved")))
                .andExpect(content().string(containsString("new")))
                .andExpect(content().string(containsString("inactive")));

        // Check
        assertThat(userRepository.findByActive(false).size(), is(equalTo(inActiveBefore + 1)));
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore)));

        List<User> newUserList = userRepository.findByInstallationId(TEST_INSTALLATION_ID);
        assertThat(newUserList.size(), is(equalTo(1)));

        User newUser = newUserList.get(0);
        assertThat(newUser.getInstallationId(), is(equalTo(TEST_INSTALLATION_ID)));
        assertThat(newUser.getPin(), is(equalTo(TEST_PIN)));
        assertThat(newUser.getUsername(), is(equalTo(TEST_USERNAME)));
        assertThat(newUser.isActive(), is(equalTo(false)));
        assertThat(newUser.isNewUser(), is(equalTo(true)));
        assertThat(newUser.getSerialId(), is(equalTo(TEST_SERIAL_ID2)));
        assertThat(newUser.getUsernameOld(), is(equalTo(null)));
        assertThat(newUser.getPinOld(), is(equalTo(null)));
    }

    @Test
    public void testUpdateUser() throws Exception {


        // Prepare
        int inActiveBefore = userRepository.findByActive(false).size();
        int activeBefore = userRepository.findByActive(true).size();

        this.mvc.perform(get(REGISTER_USER_URL)
                        .param("username", TEST_USERNAME)
                        .param("pin", TEST_PIN)
                        .param("installationId", TEST_INSTALLATION_ID2)
                        .param("appsecret", "secretApp")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("saved")))
                .andExpect(content().string(containsString("new")))
                .andExpect(content().string(containsString("inactive")));

        User user = userRepository.findByInstallationId(TEST_INSTALLATION_ID2).get(0);
        user.setActive(true);
        userRepository.save(user);
        assertThat(userRepository.findByActive(false).size(), is(equalTo(inActiveBefore)));
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore + 1)));

        // Run
        this.mvc.perform(get(REGISTER_USER_URL)
                        .param("username", TEST_USERNAME2)
                        .param("pin", TEST_PIN2)
                        .param("installationId", TEST_INSTALLATION_ID2)
                        .param("appsecret", "secretApp")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("saved")))
                .andExpect(content().string(containsString("changed")))
                .andExpect(content().string(containsString(" active")));

        // Check
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore + 1))); // user will not be deactivated by updating data.

        List<User> newUserList = userRepository.findByInstallationId(TEST_INSTALLATION_ID2);
        assertThat(newUserList.size(), is(equalTo(1)));

        User newUser = newUserList.get(0);
        assertThat(newUser.getInstallationId(), is(equalTo(TEST_INSTALLATION_ID2)));
        assertThat(newUser.getPin(), is(equalTo(TEST_PIN2)));
        assertThat(newUser.getUsername(), is(equalTo(TEST_USERNAME2)));
        assertThat(newUser.isActive(), is(equalTo(true)));
        assertThat(newUser.isNewUser(), is(equalTo(false)));
        assertThat(newUser.getPinOld(), is(equalTo(TEST_PIN)));
        assertThat(newUser.getUsernameOld(), is(equalTo(TEST_USERNAME)));
        assertThat(newUser.getSerialId(), is(equalTo(TEST_SERIAL_ID)));

        // Run - 2: Reset Old values.
        User user2 = userRepository.findByInstallationId(TEST_INSTALLATION_ID2).get(0);
        user2.setActive(true);
        userRepository.save(user2);
        assertThat(userRepository.findByActive(false).size(), is(equalTo(0)));
        assertThat(userRepository.findByActive(true).size(), is(equalTo(1)));

        // Check - 2
        User user3 = userRepository.findByInstallationId(TEST_INSTALLATION_ID2).get(0); // re-read because newUser won't be updated.
        user3.setActive(true);
        userRepository.save(user3);
        assertThat(user3.getPinOld(), is(equalTo(null)));
        assertThat(user3.getUsernameOld(), is(equalTo(null)));

        assertThat(userRepository.findByActive(false).size(), is(equalTo(inActiveBefore)));
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore + 1)));
    }

    @Test
    @Ignore("Test fails like this. Why is the user not recognised?")
    public void testRegisterUser2() throws Exception {

        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken((Principal) () -> "user", "user");

        SecurityContext mockSecurityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(mockSecurityContext.getAuthentication()).thenReturn(principal);

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                mockSecurityContext);

        this.mvc.perform(get(FrontendAPIRestController.FRONTEND_URL + "/registerUser?username=abc&pin=1111&installationId=123456789&appsecret=secretApp").session(mockHttpSession))
                .andExpect(status().is4xxClientError());
    }
}
