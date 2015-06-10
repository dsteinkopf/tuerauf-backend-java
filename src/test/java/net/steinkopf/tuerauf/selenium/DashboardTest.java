package net.steinkopf.tuerauf.selenium;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import net.steinkopf.tuerauf.TestConstants;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.controller.DashboardController;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import net.steinkopf.tuerauf.util.SeleniumHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.servlet.ServletContext;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


/**
 * Tests Dashboard
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class DashboardTest /*extends FluentTest*/ {

    private static final Logger logger = LoggerFactory.getLogger(DashboardTest.class);

    protected WebDriver driver;

    @Value("${local.server.port}")
    private int serverPort;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServletContext servletContext;


    private String getUrl() {
        return "http://" + TestConstants.ADMIN_USERNAME + ":" + TestConstants.ADMIN_PASSWORD + "@localhost:" + serverPort + servletContext.getContextPath();
    }

    public DashboardTest() {
        super();
    }

    @Before
    public void setup() {

        final BrowserVersion browserVersion = BrowserVersion.FIREFOX_24;
        driver = new HtmlUnitDriver(browserVersion);
        ((HtmlUnitDriver) driver).setJavascriptEnabled(true);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

    /**
     * Tests if users are shown in the list
     */
    @Test
    public void testUserlist() {

        // Prepare
        final String url = getUrl() + DashboardController.DASHBOARD_URL + "/";
        logger.debug("testUserlist: url={}", url);

        // Run
        driver.get(url);
        logger.debug("testUserlist: content = {}", driver.getPageSource());

        // Check
        final WebElement table = driver.findElement(By.className("users"));
        assertThat(table.getText(), containsString(TestConstants.USER_NAME_INACTIVE));
        assertThat(table.getText(), containsString(TestConstants.USER_NAME_ACTIVE));
    }

    /**
     * Tests if the activate all users button exists and works
     */
    @Test
    public void testActivateAllUsersButton() {

        // Prepare
        final String url = getUrl() + DashboardController.DASHBOARD_URL + "/";
        logger.debug("testActivateAllUsersButton: url={}", url);
        driver.get(url);
        logger.debug("testActivateAllUsersButton: content = {}", driver.getPageSource());

        {
            // Run
            final WebElement buttonContainer = driver.findElement(By.id("activateAllNewForm"));
            buttonContainer.findElement(By.name("submit")).click();

            SeleniumHelper.waitForComponentWithText(driver, "successfully activated all new users");
            logger.debug("testActivateAllUsersButton: content = {}", driver.getPageSource());

            // Check
            assertThat(driver.findElement(By.tagName("h1")).getText(), containsString("Dashboard"));
            assertThat(driver.getCurrentUrl(), not(containsString("activate"))); // should be back on normal dashboard url.
            User testUserInactive = userRepository.findOne(TestConstants.USER_ID_INACTIVE); // the one that WAS inactive at init.
            assertThat(testUserInactive.isActive(), is(equalTo(true)));

            final WebElement flashMessage = driver.findElement(By.id("flash-message"));
            assertThat(flashMessage.getText(), containsString("\n" + TestConstants.USER_NAME_INACTIVE));
            assertThat(flashMessage.getText(), not(containsString("\n" + TestConstants.USER_NAME_ACTIVE)));
        }
        {
            // Run 2: no inactive users:
            final WebElement buttonContainer = driver.findElement(By.id("activateAllNewForm"));
            buttonContainer.findElement(By.name("submit")).click();

            SeleniumHelper.waitForComponentWithText(driver, "successfully activated all new users");
            logger.debug("testActivateAllUsersButton: content = {}", driver.getPageSource());

            // Check 2
            assertThat(driver.findElement(By.tagName("h1")).getText(), containsString("Dashboard"));
            assertThat(driver.getCurrentUrl(), not(containsString("activate"))); // should be back on normal dashboard url.

            final WebElement flashMessage = driver.findElement(By.id("flash-message"));
            assertThat(flashMessage.getText(), not(containsString("\n" + TestConstants.USER_NAME_INACTIVE)));
            assertThat(flashMessage.getText(), not(containsString("\n" + TestConstants.USER_NAME_ACTIVE)));
            assertThat(flashMessage.getText(), containsString("no inactive users"));
        }
    }
}
