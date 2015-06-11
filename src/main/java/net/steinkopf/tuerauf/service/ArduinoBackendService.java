package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * Communication with arduino.
 */
@Service
public class ArduinoBackendService {

    private static final Logger logger = LoggerFactory.getLogger(ArduinoBackendService.class);

    public static final int ARDUINO_MAX_RESULT_LEN = 2000;


    @Value("${tuerauf.arduino-base-url}")
    private String arduinoBaseUrl;

    @Autowired
    private LogAndMailService logAndMailService;

    @Autowired
    private HttpFetcherService httpFetcherService;


    /**
     * Fetch Arduino's status.
     *
     * @return status string.
     */
    public String getStatus() {

        try {
            final String arduinoUrl = arduinoBaseUrl + "status";
            return httpFetcherService.fetchFromUrl(arduinoUrl, ARDUINO_MAX_RESULT_LEN);

        } catch (IOException e) {
            logAndMailService.logAndMail("Error while fetching status from Arduino", e);
            return "bad request";
        }
    }

    /**
     * Calls arduino to open the door
     *
     * @param user         user who wants to open the door.
     * @param pin          the pin that was entered by the user (NOT the stored user pin).
     * @param isNearToHome true, if user is (very) near, so simple pin is sufficient.  @return Arduino's response.
     */
    public String openDoor(final User user, final String pin, final boolean isNearToHome) {

        logger.trace("openDoor(user={}, pin={}, isNeaToHome={})", user, pin, isNearToHome);
        Assert.notNull(user, "user must not be null");
        Assert.notNull(pin, "pin must not be null");

        String arduinoUrl = arduinoBaseUrl + pin + "/" + user.getSerialId();
        if (isNearToHome) {
            arduinoUrl += "/near";
        }

        try {
            final String arduinoResponse = httpFetcherService.fetchFromUrl(arduinoUrl, 2000);
            logAndMailService.logAndMail("user {} got arduino response '{}' (serialId={})",
                    null,
                    user.getUsername(), arduinoResponse, user.getSerialId());
            return arduinoResponse;

        } catch (IOException e) {
            logAndMailService.logAndMail("Error while sending openDoor to Arduino", e);
            return "bad request";
        }
    }

    /**
     * sends pins to Arduino.
     *
     * @param pinList Array of sparsely filled pins. Index = serialId.
     * @return number of pins sent. 0 if arduino return error, -1 .
     * @throws IOException on communication or arduino error (which is already logged and mailed to admin).
     */
    public int sendPinsToArduino(String[] pinList) throws IOException {

        final StringBuilder pins4arduino = new StringBuilder();
        int pinCount = 0;
        for (int serialId = 0; serialId < pinList.length; serialId ++) {
            final String pin = pinList[serialId];
            if (serialId >= 1) {
                pins4arduino.append("&");
            }
            if (pin != null) {
                pins4arduino.append(pin);
                pinCount++;
            }
            else {
                pins4arduino.append("0");
            }
        }

        @SuppressWarnings("SpellCheckingInspection")
        final String arduinoUrl = arduinoBaseUrl + "storepinlist?" + pins4arduino;

        try {
            final String arduinoResponse = httpFetcherService.fetchFromUrl(arduinoUrl, 2000);
            if ( ! arduinoResponse.equals("done")) {
                throw new IOException(String.format("arduino returned instead of 'done': '%s'", arduinoResponse));
            }
            logAndMailService.logAndMail("sent {} pins to arduino", null, pinCount);
            return pinCount;

        } catch (IOException e) {
            logger.error("Exception while talking to arduino. Stacktrace:", e);
            logAndMailService.logAndMail("Exception while sending pins to arduino", e);
            throw e;
        }
    }

    public void setArduinoBaseUrl(final String arduinoBaseUrl) {
        this.arduinoBaseUrl = arduinoBaseUrl;
    }

    public void setLogAndMailService(final LogAndMailService logAndMailService) {
        this.logAndMailService = logAndMailService;
    }

    public void setHttpFetcherService(final HttpFetcherService httpFetcherService) {
        this.httpFetcherService = httpFetcherService;
    }

    public LogAndMailService getLogAndMailService() {
        return logAndMailService;
    }

    public HttpFetcherService getHttpFetcherService() {
        return httpFetcherService;
    }

}
