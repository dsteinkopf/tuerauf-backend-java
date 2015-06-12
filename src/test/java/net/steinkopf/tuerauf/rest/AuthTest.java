package net.steinkopf.tuerauf.rest;

import net.steinkopf.tuerauf.TestConstants;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.controller.DashboardController;
import net.steinkopf.tuerauf.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.servlet.ServletContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testing correct authentication and authorization behaviour.
 */
// @WebIntegrationTest(randomPort = true) // makes Tomcat run and listen on port

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@DirtiesContext
public class AuthTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthTest.class);


    @Value("${local.server.port}")
    private int port = 8080;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private ServletContext servletContext;


    @Before
    public void setup() {
        if (port == 0) {
            port = 8080;
        }
    }

    public void doAuthTest(String user, String password, String urlPart, HttpStatus expectedStatus, String expectedSubString) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        // headers.setAccept(Arrays.asList(MediaType.TEXT_HTML));

        TestUtils.addBasicAuthHeader(headers, password, user);

        final String url = "http://localhost:" + this.port + servletContext.getContextPath() + urlPart;
        logger.debug("doAuthTest is calling {}", url);

        HttpMethod method = urlPart.startsWith(FrontendAPIRestController.FRONTEND_URL) ? HttpMethod.POST : HttpMethod.GET;

        ResponseEntity < String > entity = new TestRestTemplate().exchange(
                url,
                method,
                new HttpEntity<Void>(headers),
                String.class);
        assertEquals(expectedStatus, entity.getStatusCode());
        if (expectedSubString != null) {
            assertTrue("Wrong body: " + entity.getBody(), entity.getBody().contains(expectedSubString));
        }
    }

    @Test
    public void authTest() throws Exception {

        String registerUserUrl = FrontendAPIRestController.FRONTEND_URL + "/registerUser?username=test&pin=1111&installationId=99999999999";
        String registerUserUrlWithAppSecret = registerUserUrl + "&appsecret=secretApp";

        String successfulResult = "saved: new inactive";
        String successfulUpdateResult = "saved: changed inactive";

        doAuthTest(null, null, registerUserUrl, HttpStatus.UNAUTHORIZED, null);
        doAuthTest(TestConstants.USER_USERNAME, TestConstants.USER_USERNAME, registerUserUrl, HttpStatus.UNAUTHORIZED, null);
        doAuthTest(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD, registerUserUrl, HttpStatus.UNAUTHORIZED, null);

        doAuthTest(null,    null,    registerUserUrlWithAppSecret, HttpStatus.OK, successfulResult);
        doAuthTest(TestConstants.USER_USERNAME, TestConstants.USER_USERNAME,   registerUserUrlWithAppSecret, HttpStatus.OK, successfulUpdateResult);
        doAuthTest(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD, registerUserUrlWithAppSecret, HttpStatus.OK, successfulUpdateResult);

        doAuthTest(null, null, "/users/", HttpStatus.UNAUTHORIZED, null);
        doAuthTest(TestConstants.USER_USERNAME, TestConstants.USER_USERNAME, "/users/", HttpStatus.FORBIDDEN, null);
        doAuthTest(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD, "/users/", HttpStatus.OK, "users");

        doAuthTest(null, null, "/", HttpStatus.UNAUTHORIZED, null);
        doAuthTest(TestConstants.USER_USERNAME, TestConstants.USER_USERNAME, "/", HttpStatus.OK, "users");
        doAuthTest(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD, "/", HttpStatus.OK, "users");

/*
        doAuthTest(null,    null,    "/admin/", HttpStatus.UNAUTHORIZED, null);
        doAuthTest("user",  "user",  "/admin/", HttpStatus.FORBIDDEN, null);
        doAuthTest("admin", "admin", "/admin/", HttpStatus.FOUND, null);
        doAuthTest("admin", "admin", "/admin/dashboard", HttpStatus.OK, "LightAdmin");
*/

        doAuthTest(null,    null,    DashboardController.DASHBOARD_URL + "/", HttpStatus.UNAUTHORIZED, null);
        doAuthTest("user",  "user",  DashboardController.DASHBOARD_URL + "/", HttpStatus.FORBIDDEN, null);
        doAuthTest("admin", "admin", DashboardController.DASHBOARD_URL + "/", HttpStatus.OK, "Dashboard");
    }
}
