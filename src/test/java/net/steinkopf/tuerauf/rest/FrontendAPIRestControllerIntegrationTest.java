package net.steinkopf.tuerauf.rest;

import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import net.steinkopf.tuerauf.SecurityContextTest;
import net.steinkopf.tuerauf.TestConstants;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import net.steinkopf.tuerauf.service.HttpFetcherService;
import net.steinkopf.tuerauf.service.LocationService;
import net.steinkopf.tuerauf.service.LogAndMailService;
import net.steinkopf.tuerauf.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link FrontendAPIRestController} with very little mocking.
 */
@SuppressWarnings({ "SpringJavaInjectionPointsAutowiringInspection", "ConstantConditions" })
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@WebAppConfiguration
@DirtiesContext
public class FrontendAPIRestControllerIntegrationTest extends SecurityContextTest {

    private static final Logger logger = LoggerFactory.getLogger(FrontendAPIRestControllerIntegrationTest.class);


    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    /**
     * System under Test
     */
    @Autowired
    private FrontendAPIRestController frontendAPIRestController;

    private MockMvc mvc;

    private HttpFetcherService mockHttpFetcherService;
    // not necessary because of @DirtiesContext: private HttpFetcherService origHttpFetcherService;

    private LocationService mockLocationService;
    // not necessary because of @DirtiesContext: private LocationService origLocationService;

    @SuppressWarnings("FieldCanBeLocal")
    private LogAndMailService mockLogAndMailService;
    // not necessary because of @DirtiesContext: private LogAndMailService origLogAndMailService;

    private final static String REGISTER_USER_URL = FrontendAPIRestController.FRONTEND_URL + "/registerUser";
    private final static String OPEN_DOOR_URL = FrontendAPIRestController.FRONTEND_URL + "/openDoor";

    private final static String TEST_INSTALLATION_ID = "1234567890";
    private final static String TEST_INSTALLATION_ID2 = "1234567891";
    private final static String TEST_PIN = "1111";
    private final static String TEST_PIN2 = "2222";
    private final static String TEST_USERNAME = "New Username";
    private final static String TEST_USERNAME2 = "Updated Username";

    private final static String arduinoBaseUrlDummy = "dummy/";


    @Before
    public void setUp() throws Exception {

        super.setup();

        this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();

        mockHttpFetcherService = Mockito.mock(HttpFetcherService.class, withSettings().invocationListeners(TestUtils.getLoggingMockInvocationListener(logger)));
        mockLocationService = Mockito.mock(LocationService.class, withSettings().invocationListeners(TestUtils.getLoggingMockInvocationListener(logger)));
        mockLogAndMailService = Mockito.mock(LogAndMailService.class, withSettings().invocationListeners(TestUtils.getLoggingMockInvocationListener(logger)));

        // not necessary because of @DirtiesContext: origLocationService = frontendAPIRestController.getLocationService();
        frontendAPIRestController.setLocationService(mockLocationService);

        // not necessary because of @DirtiesContext: origLogAndMailService = frontendAPIRestController.getArduinoBackendService().getLogAndMailService();
        frontendAPIRestController.getArduinoBackendService().setLogAndMailService(mockLogAndMailService); // only to make it quiet here

        // not necessary because of @DirtiesContext: origHttpFetcherService = frontendAPIRestController.getArduinoBackendService().getHttpFetcherService();
        frontendAPIRestController.getArduinoBackendService().setHttpFetcherService(mockHttpFetcherService);

        frontendAPIRestController.getArduinoBackendService().setArduinoBaseUrl(arduinoBaseUrlDummy);
    }

    @After
    public void tearDown() {

        // delete users created by any test.
        // those from import.sql must not be deleted.
        Stream.of(0L, 4L, 5L, 6L)
                .map(id -> userRepository.findOne(id))
                .filter(Objects::nonNull)
                .forEach(user -> userRepository.delete(user));

/* not necessary because of @DirtiesContext:
        frontendAPIRestController.setLocationService(origLocationService);
        frontendAPIRestController.getArduinoBackendService().setLogAndMailService(origLogAndMailService);
        frontendAPIRestController.getArduinoBackendService().setHttpFetcherService(origHttpFetcherService);
*/
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

        Optional<User> newUserOptional = userRepository.findByInstallationId(TEST_INSTALLATION_ID);
        assertTrue(newUserOptional.isPresent());

        final User newUser = newUserOptional.get();
        assertThat(newUser.getInstallationId(), is(equalTo(TEST_INSTALLATION_ID)));
        assertThat(newUser.getPin(), is(equalTo(TEST_PIN)));
        assertThat(newUser.getUsername(), is(equalTo(TEST_USERNAME)));
        assertThat(newUser.isActive(), is(equalTo(false)));
        assertThat(newUser.isNewUser(), is(equalTo(true)));
        // might be anything when running multiple test cases assertThat(newUser.getSerialId(), is(equalTo(TEST_SERIAL_ID2)));
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

        assertThat(userRepository.findByActive(false).size(), is(equalTo(inActiveBefore + 1)));
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore)));

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
                .andExpect(content().string(containsString(" inactive")));

        // Check
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore)));

        Optional<User> newUserOptional = userRepository.findByInstallationId(TEST_INSTALLATION_ID2);
        assertTrue(newUserOptional.isPresent());

        User newUser = newUserOptional.get();
        assertThat(newUser.getInstallationId(), is(equalTo(TEST_INSTALLATION_ID2)));
        assertThat(newUser.getPin(), is(equalTo(TEST_PIN2)));
        assertThat(newUser.getUsername(), is(equalTo(TEST_USERNAME2)));
        assertThat(newUser.isActive(), is(equalTo(false)));
        assertThat(newUser.isNewUser(), is(equalTo(true))); // user remains "new" when updated
        assertThat(newUser.getPinOld(), is(equalTo(TEST_PIN)));
        assertThat(newUser.getUsernameOld(), is(equalTo(TEST_USERNAME)));
        assertThat(newUser.getSerialId(), is(notNullValue())); // cannot check specific value because this might change

        // Run - 2: Reset Old values.
        User user2 = userRepository.findByInstallationId(TEST_INSTALLATION_ID2).get();
        user2.setActive(true);
        userRepository.save(user2);
        assertThat(userRepository.findByActive(false).size(), is(equalTo(inActiveBefore)));
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore+1)));

        // Check - 2
        User user3 = userRepository.findByInstallationId(TEST_INSTALLATION_ID2).get(); // re-read because newUser won't be updated.
        assertThat(user3.getPinOld(), is(equalTo(null)));
        assertThat(user3.getUsernameOld(), is(equalTo(null)));
        assertThat(user3.isActive(), is(equalTo(true)));
        assertThat(user3.isNewUser(), is(equalTo(false)));

        assertThat(userRepository.findByActive(false).size(), is(equalTo(inActiveBefore)));
        assertThat(userRepository.findByActive(true).size(), is(equalTo(activeBefore + 1)));
    }

    @Test
    public void testDuplicateUser() throws Exception {


        // Prepare TODO
        //noinspection unused
        int inActiveBefore = userRepository.findByActive(false).size();
        //noinspection unused
        int activeBefore = userRepository.findByActive(true).size();

        this.mvc.perform(get(REGISTER_USER_URL)
                .param("username", TEST_USERNAME)
                .param("pin", TEST_PIN)
                .param("installationId", TEST_INSTALLATION_ID)
                .param("appsecret", "secretApp")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("saved")));

        // Run
        this.mvc.perform(get(REGISTER_USER_URL)
                .param("username", TEST_USERNAME) // same username
                .param("pin", TEST_PIN2)
                .param("installationId", TEST_INSTALLATION_ID2) // different installation id
                .param("appsecret", "secretApp")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("duplicateUsername")))
                .andExpect(content().string(containsString(TEST_USERNAME)));
    }

    // @Test
    @Ignore("Test fails like this. Why is the user not recognised?")
    public void testRegisterUser2() throws Exception {

        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken((Principal) () -> "user", "user");

        SecurityContext mockSecurityContext = Mockito.mock(SecurityContext.class, withSettings().invocationListeners(TestUtils.getLoggingMockInvocationListener(logger)));
        Mockito.when(mockSecurityContext.getAuthentication()).thenReturn(principal);

        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                mockSecurityContext);

        this.mvc.perform(get(FrontendAPIRestController.FRONTEND_URL + "/registerUser?username=abc&pin=1111&installationId=123456789&appsecret=secretApp").session(mockHttpSession))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testOpenDoor() throws Exception {

        // Prepare
        User user = userRepository.findOne(TestConstants.USER_ID_ACTIVE);
        assertNotNull(user);

        final String pin = "2648";

        when(mockLocationService.isNearToHomeOuter(anyDouble(), anyDouble())).thenReturn(true);
        when(mockLocationService.isNearToHome(anyDouble(), anyDouble())).thenReturn(true);
        when(mockHttpFetcherService.fetchFromUrl(eq(arduinoBaseUrlDummy + pin + "/" + user.getSerialId() + "/near"), anyInt()))
                .thenReturn("OFFEN");

        // Run
        this.mvc.perform(get(OPEN_DOOR_URL)
                        .param("pin", pin)
                        .param("installationId", user.getInstallationId())
                        .param("geoy", "1.23")
                        .param("geox", "1.23")
                        .param("appsecret", "secretApp")
        )
                .andExpect(status().isOk())
                .andExpect(content().string(is(equalTo("OFFEN"))));

        // Check
    }
}
