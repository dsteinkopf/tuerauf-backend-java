package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.TestConstants;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import net.steinkopf.tuerauf.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext
public class ArduinoBackendServiceTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ArduinoBackendServiceTest.class);


    private final static String arduinoBaseUrlDummy = "dummy/";

    /**
     * System under test.
     */
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    ArduinoBackendService arduinoBackendService;

    private HttpFetcherService mockHttpFetcherService;
    private HttpFetcherService origHttpFetcherService;

    private LogAndMailService mockLogAndMailService;
    private LogAndMailService origLogAndMailService;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    UserRepository userRepository;


    @Before
    public void setUp() throws Exception {

        mockHttpFetcherService = Mockito.mock(HttpFetcherService.class, withSettings().invocationListeners(TestUtils.getLoggingMockInvocationListener(logger)));
        mockLogAndMailService = Mockito.mock(LogAndMailService.class, withSettings().invocationListeners(TestUtils.getLoggingMockInvocationListener(logger)));

        origHttpFetcherService = arduinoBackendService.getHttpFetcherService();
        arduinoBackendService.setHttpFetcherService(mockHttpFetcherService);

        origLogAndMailService = arduinoBackendService.getLogAndMailService();
        arduinoBackendService.setLogAndMailService(mockLogAndMailService);

        arduinoBackendService.setArduinoBaseUrl(arduinoBaseUrlDummy);
    }

    @After
    public void tearDown() throws Exception {

        arduinoBackendService.setLogAndMailService(origLogAndMailService);
        arduinoBackendService.setHttpFetcherService(origHttpFetcherService);
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
        verify(mockLogAndMailService, times(0)).logAndMail(any(String.class), any(Exception.class));
    }

    @Test
    public void testGetStatusWithException() throws IOException {

        // Mock dependencies
        when(mockHttpFetcherService.fetchFromUrl(any(String.class), anyInt())).thenThrow(new IOException("test"));

        // run
        final String statusResult = arduinoBackendService.getStatus();

        // Verify
        assertThat(statusResult, is(equalTo("bad request")));

        verify(mockLogAndMailService, times(1)).logAndMail(any(String.class), any(Exception.class));
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
        verify(mockLogAndMailService, times(1)).logAndMail(any(String.class), any(Exception.class),
                any(String.class), any(String.class), any(String.class));
        verify(mockHttpFetcherService, times(1)).fetchFromUrl(any(String.class), anyInt());

        // Run - step 2: User entered dyncode
        final String openDoorResult2 = arduinoBackendService.openDoor(user, dyncode, false);

        // Check
        assertThat(openDoorResult2, is(equalTo("OFFEN")));
        verify(mockLogAndMailService, times(2)).logAndMail(any(String.class), any(Exception.class),
                any(String.class), any(String.class), any(String.class));
        verify(mockHttpFetcherService, times(2)).fetchFromUrl(any(String.class), anyInt());
    }

    @Test
    public void testSendPinsToArduino() throws Exception {

        // Prepare
        final String[] pinList = {null, "1", "2", null, null, "5", null, null};

        // Mock dependencies
        //noinspection SpellCheckingInspection
        when(mockHttpFetcherService.fetchFromUrl(contains("storepinlist?0&1&2&0&0&5&0&0"), anyInt()))
                .thenReturn("done");

        // Run - step 1: User entered pin
        final int pinsSent = arduinoBackendService.sendPinsToArduino(pinList);

        // Check
        assertThat(pinsSent, is(equalTo(3)));
        verify(mockLogAndMailService, times(1)).logAndMail(any(String.class), any(Exception.class), anyInt());
        verify(mockHttpFetcherService, times(1)).fetchFromUrl(any(String.class), anyInt());
    }
}
