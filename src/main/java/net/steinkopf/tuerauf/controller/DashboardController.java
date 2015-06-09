package net.steinkopf.tuerauf.controller;


import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Main Info for Tuerauf-Admins.
 */
@Controller
@Secured({"ROLE_ADMIN"})
@RequestMapping(DashboardController.DASHBOARD_URL)
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);


    public static final String DASHBOARD_URL = "/dashboard";
    public static final String DASHBOARD_VIEW = "dashboard";

    public static final String MESSAGE = "message";


    @Autowired
    UserService userService;


    //@Value("${application.message:Hello World default value}")
    //private String message = "Hello World assigned value";


    /**
     * Display the dashboard (without any action).
     */
    @RequestMapping("/")
    public String dashboard(Map<String, Object> model, WebRequest webRequest) {

        // model.put(MESSAGE, "First time");
        //logger.debug("request.getURI() = {}", webRequest.);

        return DASHBOARD_VIEW;
    }

    /**
     * activate All New users now.
     */
    @RequestMapping(value = "/activateAllNew", method = RequestMethod.POST)
    public String activateAllNew(RedirectAttributes attr, HttpSession session) {

        List<User> activatedUserList = userService.activateAllNew();
        if (activatedUserList.size() >= 1) {
            final String usernames = activatedUserList.stream().map(User::getUsername).collect(Collectors.joining("<br>\n"));
            attr.addFlashAttribute(MESSAGE, "successfully activated all new users:<br>\n" + usernames);
        }
        else {
            attr.addFlashAttribute(MESSAGE, "no inactive users");
        }

        return "redirect:" + DASHBOARD_URL + "/";
    }
}
