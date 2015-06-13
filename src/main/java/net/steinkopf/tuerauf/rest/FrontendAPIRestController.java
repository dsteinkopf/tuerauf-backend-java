package net.steinkopf.tuerauf.rest;

import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.service.ArduinoBackendService;
import net.steinkopf.tuerauf.service.LocationService;
import net.steinkopf.tuerauf.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = FrontendAPIRestController.FRONTEND_URL)
// restricted by AppsecretChecker: @Secured({"ROLE_USER"})
public class FrontendAPIRestController {

    private static final Logger logger = LoggerFactory.getLogger(FrontendAPIRestController.class);


    public static final String FRONTEND_URL = "/frontend";
    public static final String FRONTEND_URL_PATTERN = FRONTEND_URL + "/**";


    @Autowired
    private UserService userService;

    @Autowired
    private ArduinoBackendService arduinoBackendService;

    @Autowired
    LocationService locationService;


    /**
     * Creates a user object or - if installationId exists - updates this user.
     * Should not be used in prod environments (in order to avoid logging of the get values).
     *
     * @return a string indicating success: "saved: new/changed active/inactive"
     */
    @RequestMapping(value = "registerUser", method = RequestMethod.GET)
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("pin") String pin,
                               @RequestParam("installationId") String installationId)
            throws Exception {

        logger.debug("registerUser(installationId={}, username={})", installationId, username);

        final User user = userService.registerOrUpdateUser(username, pin, installationId);

        return "saved:"
                + (user.isNewUser() ? " new" : " changed")
                + (user.isActive() ? " active" : " inactive");
    }

    @RequestMapping(value = "registerUser", method = RequestMethod.POST)
    public String registerUserPost(@RequestParam("username") String username,
                                   @RequestParam("pin") String pin,
                                   @RequestParam("installationId") String installationId)
            throws Exception {

        return registerUser(username, pin, installationId);
    }

    /**
     * Opens the door if user is allowed.
     * Should not be used in prod environments (in order to avoid logging of the get values).
     *
     * @return a string indicating success: e.g. "OFFEN".  "user unknown" if user is unknown or inactive.
     */
    @RequestMapping(value = "openDoor", method = RequestMethod.GET)
    public String openDoor(@RequestParam("pin") String pin,
                           @RequestParam("installationId") String installationId,
                           @RequestParam("geoy") String geoyString,
                           @RequestParam("geox") String geoxString) {

        logger.debug("openDoor(installationId={}, geoyString={}, geoxString={})", installationId, geoyString, geoxString);

        final User user = userService.getUserIfActive(installationId);
        if (user == null) {
            return "user unknown";
        }

        if (installationId.equals("monitoring")) {
            return arduinoBackendService.getStatus();
        }

        final double geoy = Double.parseDouble(geoyString);
        final double geox = Double.parseDouble(geoxString);
        final boolean isNearToHome = locationService.isNearToHome(geoy, geox);
        final boolean isNearToHomeOuter = locationService.isNearToHomeOuter(geoy, geox);

        if (!isNearToHomeOuter) {
            return "not here";
        }

        // all checks done - do arduino call now:

        final String arduinoResponse = arduinoBackendService.openDoor(user, pin, isNearToHome);
        logger.trace("openDoor: arduino returned '{}'", arduinoResponse);

        return arduinoResponse;
    }

    @RequestMapping(value = "openDoor", method = RequestMethod.POST)
    public String openDoorPost(@RequestParam("pin") String pin,
                               @RequestParam("installationId") String installationId,
                               @RequestParam("geoy") String geoyString,
                               @RequestParam("geox") String geoxString) {

        return openDoor(pin, installationId, geoyString, geoxString);

    }

    /**
     * Checks if user is "near".
     * Should not be used in prod environments (in order to avoid logging of the get values).
     *
     * @return "near" or "far". "user unknown" if user is unknown or inactive.
     */
    @RequestMapping(value = "checkLocation", method = RequestMethod.GET)
    public String checkLocation(@RequestParam("installationId") String installationId,
                                @RequestParam("geoy") String geoyString,
                                @RequestParam("geox") String geoxString) {

        logger.trace("checkLocation(installationId={}, geoyString={}, geoxString={})", installationId, geoyString, geoxString);

        final User user = userService.getUserIfActive(installationId);
        if (user == null) {
            return "user unknown";
        }

        final double geoy = Double.parseDouble(geoyString);
        final double geox = Double.parseDouble(geoxString);
        final boolean isNearToHome = locationService.isNearToHome(geoy, geox);

        final String response = isNearToHome ? "near" : "far";
        logger.trace("checkLocation: returns '{}'", response);

        return response;
    }

    @RequestMapping(value = "checkLocation", method = RequestMethod.POST)
    public String checkLocationPost(@RequestParam("installationId") String installationId,
                                    @RequestParam("geoy") String geoyString,
                                    @RequestParam("geox") String geoxString) {

        return checkLocation(installationId, geoyString, geoxString);
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

    public LocationService getLocationService() {
        return locationService;
    }

    public void setUserService(final UserService userService) {
        this.userService = userService;
    }
}
