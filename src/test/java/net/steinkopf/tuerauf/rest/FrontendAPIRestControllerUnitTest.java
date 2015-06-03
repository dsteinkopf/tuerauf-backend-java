package net.steinkopf.tuerauf.rest;

import net.steinkopf.tuerauf.TestConstants;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import net.steinkopf.tuerauf.service.ArduinoBackendService;
import net.steinkopf.tuerauf.service.LocationService;
import net.steinkopf.tuerauf.service.UserService;
import net.steinkopf.tuerauf.util.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/**
 * Tests for {@link FrontendAPIRestController}.
 * Dependency services are mocked and injected.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@WebAppConfiguration
public class FrontendAPIRestControllerUnitTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(FrontendAPIRestControllerUnitTest.class);

    /**
     * Component under Test.
     */
    // @Autowired
    private FrontendAPIRestController testedFrontendAPIRestController;

    private ArduinoBackendService mockArduinoBackendService;

    private LocationService mockLocationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private UserService userService;


    @Before
    public void setUp() throws Exception {

        mockArduinoBackendService = Mockito.mock(ArduinoBackendService.class, withSettings().invocationListeners(Utils.getLoggingMockInvocationListener(logger)));
        mockLocationService = Mockito.mock(LocationService.class, withSettings().invocationListeners(Utils.getLoggingMockInvocationListener(logger)));

        testedFrontendAPIRestController = new FrontendAPIRestController();
        testedFrontendAPIRestController.setUserService(userService);
        testedFrontendAPIRestController.setArduinoBackendService(mockArduinoBackendService);
        testedFrontendAPIRestController.setLocationService(mockLocationService);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void openDoorTestUnknownUser() throws Exception {

        // Prepare
        final String geoy = "1.23";
        final String geox = "1.23";
        final String pin = "3437";

        // Run
        final String result = testedFrontendAPIRestController.openDoor(pin, "DoesNotExist", geoy, geox);

        // Check
        assertThat(result, is(equalTo("user unknown")));

        verify(mockArduinoBackendService, times(0)).openDoor(any(User.class), anyString(), anyBoolean()); // no calls because user does not exist.
    }

    @Test
    public void openDoorTestUserInactive() throws Exception {

        // Prepare
        final String geoy = "1.23";
        final String geox = "1.23";
        final String pin = "3437";

        User user = userRepository.findOne(TestConstants.USER_ID_INACTIVE);
        assertNotNull(user);

        // Run
        final String result = testedFrontendAPIRestController.openDoor(pin, user.getInstallationId(), geoy, geox);

        // Check
        assertThat(result, is(equalTo("user unknown")));

        verify(mockArduinoBackendService, times(0)).openDoor(any(User.class), anyString(), anyBoolean()); // no calls because user is inactive.
    }

    @Test
    public void openDoorTestFarAway() throws Exception {

        // Prepare
        final String geoy = "1.23";
        final String geox = "1.23";
        final String pin = "3437";

        User user = userRepository.findOne(TestConstants.USER_ID_ACTIVE);
        assertNotNull(user);

        // Prepare mocking
        when(mockLocationService.isNearToHomeOuter(anyDouble(), anyDouble())).thenReturn(false);
        when(mockLocationService.isNearToHome(anyDouble(), anyDouble())).thenReturn(false);

        // Run
        final String result = testedFrontendAPIRestController.openDoor(pin, user.getInstallationId(), geoy, geox);

        // Check
        assertThat(result, is(equalTo("not here")));

        verify(mockArduinoBackendService, times(0)).openDoor(any(User.class), anyString(), anyBoolean()); // no calls because user is "not here".
    }

    @Test
    public void openDoorTestIsNearOuter() throws Exception {

        // Prepare
        final String geoy = "1.23";
        final String geox = "1.23";
        final String pin = "3437";
        final String dyncode = "9872";
        final String fakeResponse = "dyncode: " + dyncode;

        User user = userRepository.findOne(TestConstants.USER_ID_ACTIVE);
        assertNotNull(user);

        // Prepare mocking
        when(mockLocationService.isNearToHomeOuter(anyDouble(), anyDouble())).thenReturn(true);
        when(mockLocationService.isNearToHome(anyDouble(), anyDouble())).thenReturn(false);
        when(mockArduinoBackendService.openDoor(user, pin, false)).thenReturn(fakeResponse);

        // Run
        final String result = testedFrontendAPIRestController.openDoor(pin, user.getInstallationId(), geoy, geox);

        // Check
        assertThat(result, is(equalTo(fakeResponse)));

        verify(mockArduinoBackendService, times(1)).openDoor(any(User.class), anyString(), anyBoolean());
    }


    @Test
    public void openDoorTestIsNear() throws Exception {

        // Prepare
        final String geoy = "1.23";
        final String geox = "1.23";
        final String pin = "3437";
        final String dyncode = "9872";
        final String fakeResponse = "dyncode: " + dyncode;

        User user = userRepository.findOne(TestConstants.USER_ID_ACTIVE);
        assertNotNull(user);

        // Prepare mocking
        when(mockLocationService.isNearToHomeOuter(anyDouble(), anyDouble())).thenReturn(true);
        when(mockLocationService.isNearToHome(anyDouble(), anyDouble())).thenReturn(true);
        when(mockArduinoBackendService.openDoor(user, pin, true)).thenReturn(fakeResponse);

        // Run
        final String result = testedFrontendAPIRestController.openDoor(pin, user.getInstallationId(), geoy, geox);

        // Check
        assertThat(result, is(equalTo(fakeResponse)));

        verify(mockArduinoBackendService, times(1)).openDoor(any(User.class), anyString(), anyBoolean());
    }
}
