package net.steinkopf.tuerauf.controller;


import com.google.common.collect.Lists;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import net.steinkopf.tuerauf.service.ArduinoBackendService;
import net.steinkopf.tuerauf.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Main Info for Tuerauf-Admins.
 */
@SuppressWarnings("WeakerAccess")
@Controller
@Secured({"ROLE_ADMIN"})
@RequestMapping(DashboardController.DASHBOARD_URL)
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);


    public static final String DASHBOARD_URL = "/dashboard";
    public static final String DASHBOARD_VIEW = "dashboard";

    public static final String REDIRECT_TO_DASHBOARD = "redirect:" + DASHBOARD_URL + "/";

    public static final String MESSAGE = "message";


    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArduinoBackendService arduinoBackendService;


    @Value("${tuerauf.git-revision-hash}")
    private String gitRevisionHash;

    @Value("${tuerauf.build.timestamp}")
    private String buildTimestamp;

    @Value("${tuerauf.build.timestamp.format}")
    private String buildTimestampFormat;

    @Value("${tuerauf.appsecret}")
    private String appsecret;

    @Value("${tuerauf.external-url}")
    private String externalUrl;

    @Value("${tuerauf.prod-version}")
    private boolean prodVersion;


    private void addVersionInfo(Map<String, Object> model) {

        if (StringUtils.isNotBlank(buildTimestamp)) {
            Date buildDate = new Date(Long.valueOf(buildTimestamp));
            SimpleDateFormat sdf = new SimpleDateFormat(buildTimestampFormat); // the format of your date
            // sdf.setTimeZone(TimeZone.getTimeZone("GMT+1")); // give a timezone reference for formatting (see comment at the bottom
            String buildDateFormatted = sdf.format(buildDate);
            model.put("implementationBuildTime", buildDateFormatted);
        }
        model.put("implementationBuild", gitRevisionHash);

        model.put("javaVersion", System.getProperty("java.version"));
    }

    /**
     * Display the dashboard (without any action).
     */
    @RequestMapping("/")
    public String dashboard(Map<String, Object> model) {

        addVersionInfo(model);

        final List<User> userList = Lists.newArrayList(userRepository.findAll());
        userList.forEach(user -> {
            user.setPin(getObfuscated(user.getPin()));
            user.setPinOld(getObfuscated(user.getPinOld()));
        });
        model.put("users", userList);
        logger.debug("userList.size()={}", userList.size());

        return DASHBOARD_VIEW;
    }

    private String getObfuscated(final String pin) {
        if (pin == null) {
            return null;
        }
        //noinspection ReplaceAllDot
        return pin.replaceAll("[0-9]", "*");
    }

    /**
     * activate all new users now.
     */
    @RequestMapping(value = "/activateAllNew", method = RequestMethod.POST)
    public String activateAllNew(RedirectAttributes attr) {

        List<User> activatedUserList = userService.activateAllNew();
        if (!activatedUserList.isEmpty()) {
            final String usernames = activatedUserList.stream().map(User::getUsername).collect(Collectors.joining("<br>\n"));
            attr.addFlashAttribute(MESSAGE, "successfully activated all new users:<br>\n" + usernames);
        }
        else {
            attr.addFlashAttribute(MESSAGE, "no inactive users");
        }

        return REDIRECT_TO_DASHBOARD;
    }

    /**
     * send pins of active users to Arduino now.
     */
    @RequestMapping(value = "/sendPinsToArduino", method = RequestMethod.POST)
    public String sendPinsToArduino(@RequestParam("pinPassword") String enteredPinPassword,
                                    RedirectAttributes attr) {

        final String[] pinList = userService.getActivePinList();
        final int pinsSent;
        try {
            pinsSent = arduinoBackendService.sendPinsToArduino(enteredPinPassword, pinList);

            attr.addFlashAttribute(MESSAGE, String.format("sent %s pins to arduino", pinsSent));

            // After successfully sending to Arduino: delete all locally stored active PINs
            userService.deletePins(pinList);

        } catch (IOException | IllegalArgumentException e) {
            attr.addFlashAttribute(MESSAGE, e.toString());
        }

        return REDIRECT_TO_DASHBOARD;
    }

    /**
     * open door immediately.
     */
    @RequestMapping(value = "/openDoorImmediately", method = RequestMethod.POST)
    public String openDoorImmediately(@RequestParam("masterPin") String enteredMasterPin,
                                      RedirectAttributes attr) {

        try {
            final String arduinoResponse = arduinoBackendService.openDoorImmediately(enteredMasterPin);

            if (arduinoResponse.equals("OFFEN")) {
                attr.addFlashAttribute(MESSAGE, "Door is OPEN now.");
            }
            else {
                attr.addFlashAttribute(MESSAGE, String.format("Door is NOT open now. Arduino's response = '%s'",
                        arduinoResponse));
            }
        } catch (IllegalArgumentException e) {
            attr.addFlashAttribute(MESSAGE, e.toString());
        }

        return REDIRECT_TO_DASHBOARD;
    }

    /**
     * show config links.
     */
    // see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-ann-arguments
    @RequestMapping(value = "/showConfigLink", method = RequestMethod.POST)
    public String showConfigLink(RedirectAttributes attr) throws UnsupportedEncodingException {

        // e.g. tuerauf:///?https%3A%2F%2Fbackend.myhome%3A39931%2Ftuerauf%2F/MyAppsecret
        final String myUrl = externalUrl + (externalUrl.endsWith("/") ? "" : "/");
        final String prefix = prodVersion ? "tuerauf" : "tuerauftest";
        final String configLink = String.format("%s:///?%s/%s", prefix, URLEncoder.encode(myUrl, "UTF-8"), appsecret);

        attr.addFlashAttribute(MESSAGE, String.format("Secret config Link:<br>%s", configLink));

        return REDIRECT_TO_DASHBOARD;
    }

    /**
     * Join users.
     */
    @RequestMapping(value = "/joinUsers", method = RequestMethod.POST)
    public String joinUsers(
        @RequestParam("newUserId") String newUserIdString,
        @RequestParam("existingUserId") String existingUserIdString,
        RedirectAttributes attr) {

        try {
            final long newUserId = Integer.parseInt(newUserIdString);
            final long existingUserId = Integer.parseInt(existingUserIdString);

            // fetch for display before it is deleted resp. updated by join operation.
            final String newUsername = userRepository.findOne(newUserId).getUsername();
            final String existingUsername = userRepository.findOne(existingUserId).getUsername();

            userService.joinNewUserToExistingUser(newUserId, existingUserId);

            attr.addFlashAttribute(MESSAGE, String.format("Successfully joined user %s (%d) to %s (%d).",
                newUsername, newUserId, existingUsername, existingUserId));
        }
        catch (IllegalArgumentException ex) {
            attr.addFlashAttribute(MESSAGE, ex.getMessage());
        }

        return REDIRECT_TO_DASHBOARD;
    }
}
