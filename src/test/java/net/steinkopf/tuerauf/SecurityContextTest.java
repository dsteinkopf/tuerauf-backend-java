package net.steinkopf.tuerauf;

import net.steinkopf.tuerauf.service.LogAndMailService;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.security.Principal;


/**
 * Test needing some security context (= authenticated user/admin).
 */
@TestExecutionListeners(inheritListeners = false, listeners = {
        DependencyInjectionTestExecutionListener.class }) // see http://stackoverflow.com/questions/25537436/integration-testing-a-spring-boot-web-app-with-testng
public abstract class SecurityContextTest extends AbstractTestNGSpringContextTests {

    @Autowired
    protected LogAndMailService logAndMailService;


    /**
     * Name of security user needed for the test to run.
     */
    protected String getNeededSecuredUsername() {
        return "admin";
    }
    /**
     * Name of security user's password needed for the test to run.
     */
    protected String getNeededSecuredUserPassword() {
        return "admin";
    }


    /**
     * Adds a valid (authenticated) principal to SecurityContextHolder.
     */
    @Before
    public void setup() throws Exception {

        // add principal object to SecurityContextHolder
        Principal principal = new Principal() {
            @Override
            public String getName() {
                return getNeededSecuredUsername();
            }
        };
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, getNeededSecuredUserPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @After
    public void tearDown() throws Exception {
        logAndMailService.awaitTermination();
    }
}
