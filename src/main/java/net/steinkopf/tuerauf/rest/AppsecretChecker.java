package net.steinkopf.tuerauf.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Checks if a request has got the correct appsecret.
 */
@Component
//@PropertySource("classpath:application.properties")
// @ConfigurationProperties(prefix="frontend")
public class AppsecretChecker extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AppsecretChecker.class);


    @Value("${tuerauf.appsecret}")
    private String appsecret;


    public AppsecretChecker() {

        // an this moment not yet set: Assert.hasText(appsecret, "property tuerauf.appsecret is not configured.");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        String urlPattern = request.getContextPath() + FrontendAPIRestController.FRONTEND_URL_PATTERN;
        boolean isFrontendRequest = new AntPathMatcher().match(urlPattern, request.getRequestURI());
        if ( ! isFrontendRequest) {
            return true;
        }

        Assert.hasText(appsecret, "property tuerauf.appsecret is not configured.");

        String appsecretParam = request.getParameter("appsecret");
        if ( ! appsecret.equals(appsecretParam)) {
            logger.info("URL param appsecret is missing or wrong: {}", appsecretParam);
            throw new AuthenticationCredentialsNotFoundException("URL param appsecret is missing or wrong: " + appsecretParam);
        }
        return true;
    }
}

