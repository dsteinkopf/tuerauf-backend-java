package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.TestConstants;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import net.steinkopf.tuerauf.util.Utils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/**
 * Tests for ArduinoBackendService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
public class ArduinoBackendServiceTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ArduinoBackendServiceTest.class);


    private final static String arduinoBaseUrlDummy = "dummy/";

    /**
     * System under test.
     */
    @Autowired
    ArduinoBackendService arduinoBackendService;

    private HttpFetcherService mockHttpFetcherService;
    private LogAndMailService mockLogAndMailService;

    @Autowired
    UserRepository userRepository;


    @Before
    public void setUp() throws Exception {

        mockHttpFetcherService = Mockito.mock(HttpFetcherService.class, withSettings().invocationListeners(Utils.getLoggingInvocationListener(logger)));
        mockLogAndMailService = Mockito.mock(LogAndMailService.class, withSettings().invocationListeners(Utils.getLoggingInvocationListener(logger)));

        arduinoBackendService.setHttpFetcherService(mockHttpFetcherService);
        arduinoBackendService.setLogAndMailService(mockLogAndMailService);
        arduinoBackendService.setArduinoBaseUrl(arduinoBaseUrlDummy);
    }

    @Test
    public void testGetStatus() throws Exception {

        final String dummyResult = "dummy";
        // Mock dependencies
        when(mockHttpFetcherService.fetchFromUrl(any(String.class), anyInt())).thenReturn(dummyResult);

        // run
        final String statusResult = arduinoBackendService.getStatus();

        // Verify
        assertThat(statusResult, is(equalTo(dummyResult)));

        verify(mockHttpFetcherService, times(1)).fetchFromUrl(eq(arduinoBaseUrlDummy + "status"), anyInt());
        verify(mockHttpFetcherService, times(1)).fetchFromUrl(any(String.class), anyInt());
        verify(mockLogAndMailService, times(0)).logAndMail(any(String.class));
    }

    @Test
    public void testGetStatusWithException() throws IOException {

        // Mock dependencies
        when(mockHttpFetcherService.fetchFromUrl(any(String.class), anyInt())).thenThrow(new IOException("test"));

        // run
        final String statusResult = arduinoBackendService.getStatus();

        // Verify
        assertThat(statusResult, is(equalTo("bad request")));

        verify(mockLogAndMailService, times(0)).logAndMail(any(String.class));
    }

    @Test
    public void testOpenDoor() throws Exception {

        final String pin = "7776";
        final String dyncode = "8956";
        final String dummyFirstResult = "dyncode " + dyncode;

        // Prepare
        User user = userRepository.findOne(TestConstants.USER_ID_ACTIVE);
        assertNotNull(user);

        // Mock dependencies
        when(mockHttpFetcherService.fetchFromUrl(eq(arduinoBaseUrlDummy + pin + "/" + user.getSerialId()), anyInt()))
                .thenReturn(dummyFirstResult);
        when(mockHttpFetcherService.fetchFromUrl(eq(arduinoBaseUrlDummy + dyncode + "/" + user.getSerialId()), anyInt()))
                .thenReturn("OFFEN");

        // Run - step 1: User entered pin
        final String openDoorResult = arduinoBackendService.openDoor(user, pin, false);

        // Check
        assertThat(openDoorResult, is(equalTo(dummyFirstResult)));
        verify(mockLogAndMailService, times(1)).logAndMail(any(String.class), any(String.class), any(String.class), any(String.class));
        verify(mockHttpFetcherService, times(1)).fetchFromUrl(any(String.class), anyInt());

        // Run - step 2: User entered dyncode
        final String openDoorResult2 = arduinoBackendService.openDoor(user, dyncode, false);

        // Check
        assertThat(openDoorResult2, is(equalTo("OFFEN")));
        verify(mockLogAndMailService, times(2)).logAndMail(any(String.class), any(String.class), any(String.class), any(String.class));
        verify(mockHttpFetcherService, times(2)).fetchFromUrl(any(String.class), anyInt());
    }
}
