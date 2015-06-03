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
     * @return a string indicating success: "saved: new/changed active/inactive"
     */
    @RequestMapping(value = "registerUser", method = RequestMethod.GET)
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("pin") String pin,
                               @RequestParam("installationId") String installationId)
            throws Exception {

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
     * @return a string indicating success: e.g. "OFFEN"
     */
    @RequestMapping(value = "openDoor", method = RequestMethod.GET)
    public String openDoor(@RequestParam("pin") String pin,
                           @RequestParam("installationId") String installationId,
                           @RequestParam("geoy") String geoyString,
                           @RequestParam("geox") String geoxString) {

        logger.trace("openDoor(pin={}, installationId={}, geoyString={}, geoxString={})", pin, installationId, geoyString, geoxString);

        final User user = userService.getUserIfActive(installationId);
        if (user == null) {
            return "user unknown";
        }

        if (installationId.equals("monitoring")) {
            return arduinoBackendService.getStatus();
        }

        final double geoy = Float.parseFloat(geoyString);
        final double geox = Float.parseFloat(geoxString);
        final boolean isNearToHome = locationService.isNearToHome(geoy, geox);
        final boolean isNearToHomeOuter = locationService.isNearToHomeOuter(geoy, geox);

        if ( ! isNearToHomeOuter) {
            return "not here";
        }

        // all checks done - do arduino call now:

        return arduinoBackendService.openDoor(user, pin, isNearToHome);
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
