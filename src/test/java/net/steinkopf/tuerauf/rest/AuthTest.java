package net.steinkopf.tuerauf.rest;

import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.service.LogAndMailService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.charset.Charset;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Testing correct authentication and authorization behaviour.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@WebIntegrationTest(randomPort = true) // makes Tomcat run and listen on port
public class AuthTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthTest.class);


    @Value("${local.server.port}")
    private int port = 8080;

    @Before
    public void setup() {
        if (port == 0) {
            port = 8080;
        }
    }

    public void doAuthTest(String user, String password, String urlPart, HttpStatus expectedStatus, String expectedSubString) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        // headers.setAccept(Arrays.asList(MediaType.TEXT_HTML));

        if (user != null && password !=  null) {
            byte[] authString = (user + ":" + password).getBytes(Charset.defaultCharset());
            byte[] authStringBase64 = Base64.getEncoder().encode(authString);
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + new String(authStringBase64));
        }

        ResponseEntity < String > entity = new TestRestTemplate().exchange(
                "http://localhost:" + this.port + urlPart, // without context "/tuerauf".
                HttpMethod.GET,
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

        doAuthTest(null, null, registerUserUrl, HttpStatus.UNAUTHORIZED, null);
        doAuthTest("user", "user", registerUserUrl, HttpStatus.UNAUTHORIZED, null);
        doAuthTest("admin", "admin", registerUserUrl, HttpStatus.UNAUTHORIZED, null);

        doAuthTest(null,    null,    registerUserUrlWithAppSecret, HttpStatus.OK, successfulResult);
        doAuthTest("user", "user",   registerUserUrlWithAppSecret, HttpStatus.OK, successfulResult);
        doAuthTest("admin", "admin", registerUserUrlWithAppSecret, HttpStatus.OK, successfulResult);

        doAuthTest(null, null, "/users/", HttpStatus.UNAUTHORIZED, null);
        doAuthTest("user", "user", "/users/", HttpStatus.FORBIDDEN, null);
        doAuthTest("admin", "admin", "/users/", HttpStatus.OK, "users");

        doAuthTest(null, null, "/", HttpStatus.UNAUTHORIZED, null);
        doAuthTest("user", "user", "/", HttpStatus.OK, "users");
        doAuthTest("admin", "admin", "/", HttpStatus.OK, "users");

        // geht nicht, weil Test als jar läuft - nicht war... doAuthTest(null,    null,    "/admin/", HttpStatus.UNAUTHORIZED, null);
        // geht nicht, weil Test als jar läuft - nicht war... doAuthTest("user",  "user",  "/admin/", HttpStatus.FORBIDDEN, null);
        // geht nicht, weil Test als jar läuft - nicht war... doAuthTest("admin", "admin", "/admin/", HttpStatus.OK, "LightAdmin");
    }
}
