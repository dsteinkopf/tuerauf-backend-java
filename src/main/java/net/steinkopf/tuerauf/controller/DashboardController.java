package net.steinkopf.tuerauf.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.Map;


/**
 * Main Info for Tuerauf-Admins.
 */
@Controller
@Secured({"ROLE_ADMIN"})
public class DashboardController {

    public static final String DASHBOARD_URL = "/dashboard";


    @Value("${application.message:Hello World default value}")
    private String message = "Hello World assigned value";

    @RequestMapping(DASHBOARD_URL)
    public String dashboard(Map<String, Object> model) {
        model.put("time", new Date());
        model.put("message", this.message);
        return "dashboard";
    }

    @RequestMapping("/foo")
    public String foo(Map<String, Object> model) {
        throw new RuntimeException("Foo");
    }

}
