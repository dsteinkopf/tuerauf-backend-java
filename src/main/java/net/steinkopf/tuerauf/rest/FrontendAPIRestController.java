package net.steinkopf.tuerauf.rest;

import net.steinkopf.tuerauf.data.AccessLog;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.service.AccessLogService;
import net.steinkopf.tuerauf.service.ArduinoBackendService;
import net.steinkopf.tuerauf.service.LocationService;
import net.steinkopf.tuerauf.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.Optional;


@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@RestController
@RequestMapping(value = FrontendAPIRestController.FRONTEND_URL)
// restricted by AppsecretChecker: @Secured({"ROLE_USER"})
public class FrontendAPIRestController {

    private static final Logger logger = LoggerFactory.getLogger(FrontendAPIRestController.class);


    @SuppressWarnings("WeakerAccess")
    public static final String FRONTEND_URL = "/frontend";
    public static final String FRONTEND_URL_PATTERN = FRONTEND_URL + "/**";
    private static final String USER_UNKNOWN = "user unknown";

    @Autowired
    private UserService userService;

    @Autowired
    private ArduinoBackendService arduinoBackendService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private AccessLogService accessLogService;

    @Autowired
    private EntityManager entityManager;


    /**
     * Creates a user object or - if installationId exists - updates this user.
     * Should not be used in prod environments (in order to avoid logging of the get values).
     *
     * e.g.
     * http://localhost:8080/tuerauf/frontend/registerUser?installationId=0600763045345&username=test1&appsecret=secretApp&pin=1234
     *
     * @return a string indicating success: "saved: new/changed active/inactive"
     */
    @RequestMapping(value = "registerUser", method = { RequestMethod.GET, RequestMethod.POST })
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("pin") String pin,
                               @RequestParam("installationId") String installationId) {

        logger.debug("registerUser(installationId={}, username={})", installationId, username);

        try {
            final Optional<User> existingUser = userService.getUser(installationId);
            final User user = userService.registerOrUpdateUser(username.trim(), pin, installationId);

            return "saved:"
                    + (existingUser.isPresent() ? " changed" : " new")
                    + (user.isActive() ? " active" : " inactive");
        }
        catch (UserService.DuplicateUsernameException ex) {
            logger.debug("duplicateUsernameException: " + ex.getMessage());
            return "duplicateUsername: " + username;
        }
    }

    /**
     * Opens the door if user is allowed.
     * Should not be used in prod environments (in order to avoid logging of the get values).
     *
     * e.g.
     * http://localhost:8080/tuerauf/frontend/openDoor?appsecret=secretApp&installationId=testInstallation1&geoy=23.45&geox=12.34&pin=1111
     *
     * @return a string indicating success: e.g. "OFFEN".  "user unknown" if user is unknown or inactive.
     */
    @RequestMapping(value = "openDoor", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.HEAD })
    public String openDoor(@RequestParam("pin") String enteredPin,
                           @RequestParam("installationId") String installationId,
                           @RequestParam("geoy") String geoyString,
                           @RequestParam("geox") String geoxString) {

        if (installationId.equals("monitoring") || installationId.equals("haproxy")) {
            // e.g. http://localhost:8080/tuerauf/frontend/openDoor?appsecret=secretApp&installationId=monitoring&geoy=12.34567&geox=23.45678&pin=1111
            //noinspection SqlDialectInspection,SqlNoDataSourceInspection
            entityManager.createNativeQuery("select 1 from user").getSingleResult(); // check DB connection
            Assert.isTrue(userService.getUserCount() >= 1, "no existing users");
            return arduinoBackendService.getStatus();
        }

        final Optional<User> userOptional = userService.getUserIfActive(installationId);
        if ( ! userOptional.isPresent()) {
            logger.debug("openDoor(installationId={}, geoyString={}, geoxString={})", installationId, geoyString, geoxString);

            return USER_UNKNOWN;
        }
        final User user = userOptional.get();

        logger.debug("openDoor(username={}, geoyString={}, geoxString={})", user.getUsername(), geoyString, geoxString);

        final double geoy = Double.parseDouble(geoyString);
        final double geox = Double.parseDouble(geoxString);

        String result = null;
        try {
            final boolean isNearToHome = locationService.isNearToHome(geoy, geox);
            final boolean isNearToHomeOuter = locationService.isNearToHomeOuter(geoy, geox);

            if (!isNearToHomeOuter) {
                result = "not here";
                return result;
            }

            // all checks done - do arduino call now:

            final String arduinoResponse = arduinoBackendService.openDoor(user, enteredPin, isNearToHome);
            logger.trace("openDoor: arduino returned '{}'", arduinoResponse);

            result = arduinoResponse;
            return result;
        }
        catch (Exception e) {
            result = e.toString();
            throw e;
        }
        finally {
            accessLogService.log(user, AccessLog.AccessType.openDoor, geoy, geox, result);
        }
    }

    /**
     * Checks if user is "near".
     * Should not be used in prod environments (in order to avoid logging of the get values).
     *
     * e.g.
     * http://localhost:8080/tuerauf/frontend/checkLocation?appsecret=secretApp&installationId=testInstallation1&geoy=23.45&geox=12.34
     *
     * @return "near" or "far". "user unknown" if user is unknown or inactive.
     */
    @RequestMapping(value = "checkLocation", method = { RequestMethod.GET, RequestMethod.POST })
    public String checkLocation(@RequestParam("installationId") String installationId,
                                @RequestParam("geoy") String geoyString,
                                @RequestParam("geox") String geoxString) {

        final Optional<User> userOptional = userService.getUserIfActive(installationId);
        if ( ! userOptional.isPresent()) {
            logger.debug("checkLocation(installationId={}, geoyString={}, geoxString={})", installationId, geoyString, geoxString);

            return USER_UNKNOWN;
        }
        final User user = userOptional.get();

        logger.debug("checkLocation(username={}, geoyString={}, geoxString={})", user.getUsername(), geoyString, geoxString);

        final double geoy = Double.parseDouble(geoyString);
        final double geox = Double.parseDouble(geoxString);

        String result = null;
        try {
            final boolean isNearToHome = locationService.isNearToHome(geoy, geox);

            result = isNearToHome ? "near" : "far";
            logger.trace("checkLocation: returns '{}'", result);

            return result;
        }
        catch (Exception e) {
            result = e.toString();
            throw e;
        }
        finally {
            accessLogService.log(user, AccessLog.AccessType.checkLocation, geoy, geox, result);
        }
    }

    void setLocationService(final LocationService locationService) {
        this.locationService = locationService;
    }

    ArduinoBackendService getArduinoBackendService() {
        return arduinoBackendService;
    }

    void setArduinoBackendService(final ArduinoBackendService arduinoBackendService) {
        this.arduinoBackendService = arduinoBackendService;
    }
}
