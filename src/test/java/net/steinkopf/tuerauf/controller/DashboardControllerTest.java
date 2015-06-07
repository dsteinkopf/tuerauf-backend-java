package net.steinkopf.tuerauf.controller;

import net.steinkopf.tuerauf.TestConstants;
import net.steinkopf.tuerauf.TueraufApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.servlet.ServletContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for DashboardController.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
// @WebIntegrationTest(randomPort = true) // makes Tomcat run and listen on port
@DirtiesContext
public class DashboardControllerTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private ServletContext servletContext;


    @Test
    public void testDashboard() throws Exception {

        final String urlPart = DashboardController.DASHBOARD_URL;

        HttpHeaders headers = new HttpHeaders();

        ResponseEntity<String> entity = new TestRestTemplate(TestConstants.ADMIN_USERNAME, TestConstants.ADMIN_PASSWORD)
                .getForEntity("http://localhost:" + this.port + servletContext.getContextPath() + urlPart, String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertTrue("Wrong body:\n" + entity.getBody(), entity.getBody().contains("Dashboard"));
        assertTrue("Wrong body:\n" + entity.getBody(), entity.getBody().contains("Message"));
    }
}