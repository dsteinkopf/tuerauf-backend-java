package net.steinkopf.tuerauf.selenium;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import net.steinkopf.tuerauf.TestConstants;
import net.steinkopf.tuerauf.TueraufApplication;
import net.steinkopf.tuerauf.controller.DashboardController;
import net.steinkopf.tuerauf.data.User;
import net.steinkopf.tuerauf.repository.UserRepository;
import net.steinkopf.tuerauf.service.ArduinoBackendService;
import net.steinkopf.tuerauf.service.HttpFetcherService;
import net.steinkopf.tuerauf.util.SeleniumHelper;
import net.steinkopf.tuerauf.util.TestUtils;
import org.mockito.Mockito;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;


/**
 * Tests Dashboard
 */
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@TestExecutionListeners(inheritListeners = false, listeners = {
        DependencyInjectionTestExecutionListener.class }) // see http://stackoverflow.com/questions/25537436/integration-testing-a-spring-boot-web-app-with-testng
public class DashboardTest /*extends FluentTest*/ extends AbstractTestNGSpringContextTests {

    private static final Logger logger = LoggerFactory.getLogger(DashboardTest.class);

    protected WebDriver driver;

    @Value("${local.server.port}")
    private int serverPort;

    private HttpFetcherService mockHttpFetcherService;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private UserRepository userRepository;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private ServletContext servletContext;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private ArduinoBackendService arduinoBackendService;


    private String getUrl() {
        return "http://" + TestConstants.ADMIN_USERNAME + ":" + TestConstants.ADMIN_PASSWORD + "@localhost:" + serverPort + servletContext.getContextPath();
    }

    public DashboardTest() {
        super();
    }

    @BeforeClass
    public void setup() {

        final BrowserVersion browserVersion = BrowserVersion.FIREFOX_24;
        driver = new HtmlUnitDriver(browserVersion);
        ((HtmlUnitDriver) driver).setJavascriptEnabled(true);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    @AfterClass
    public void tearDown() throws Exception {
        driver.quit();
    }

    /**
     * Tests if users are shown in the list
     */
    @Test
    @DirtiesContext
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
    @Test(dependsOnMethods = "testSendPinsToArduino") // TODO not a real dependency!  testSendPinsToArduino just must not run when all users are active
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

    /**
     * Tests if the send pins to arduino button exists and works
     */
    @Test
    @DirtiesContext
    public void testSendPinsToArduino() throws Exception {

        final String dummyPinPassword = "myPinPw";
        final String arduinoBaseUrlDummy = "dummy/";

        // Prepare mocking in mockHttpFetcherService
        mockHttpFetcherService = Mockito.mock(HttpFetcherService.class,
                withSettings().invocationListeners(TestUtils.getLoggingMockInvocationListener(logger)));
        arduinoBackendService.setHttpFetcherService(mockHttpFetcherService);

        arduinoBackendService.setArduinoBaseUrl(arduinoBaseUrlDummy);

        // correctness of pins is not checked in this test
        //noinspection SpellCheckingInspection
        when(mockHttpFetcherService.fetchFromUrl(contains(arduinoBaseUrlDummy+"storepinlist?" + dummyPinPassword + ":"), anyInt()))
                .thenReturn("done");

        // Prepare
        final String url = getUrl() + DashboardController.DASHBOARD_URL + "/";
        logger.debug("testSendPinsToArduino: url={}", url);
        driver.get(url);
        logger.debug("testSendPinsToArduino: content = {}", driver.getPageSource());

        final WebElement table = driver.findElement(By.className("users"));
        assertThat(table.getText(), containsString(TestConstants.PIN_ALMOST_ACTIVE));

        {
            // Run

            final WebElement pinPasswordField = driver.findElement(By.id("pinPassword"));
            pinPasswordField.sendKeys(dummyPinPassword);

            final WebElement buttonContainer = driver.findElement(By.id("sendPinsToArduino"));
            buttonContainer.findElement(By.name("submit")).click();

            SeleniumHelper.waitForComponentWithText(driver, "sent ");
            logger.debug("testSendPinsToArduino: content = {}", driver.getPageSource());

            // Check
            final WebElement flashMessage = driver.findElement(By.id("flash-message"));
            assertThat(flashMessage.getText(), containsString("pins to arduino"));

            assertThat(driver.findElement(By.tagName("h1")).getText(), containsString("Dashboard"));
            assertThat(driver.getCurrentUrl(), not(containsString("activate"))); // should be back on normal dashboard url.
            User testUserActive = userRepository.findOne(TestConstants.USER_ID_ACTIVE);
            assertThat(testUserActive.getPin(), is(equalTo(null)));

            final WebElement tableAfter = driver.findElement(By.className("users"));
            assertThat(tableAfter.getText(), not(containsString(TestConstants.PIN_ALMOST_ACTIVE)));
            assertThat(tableAfter.getText(), containsString(TestConstants.PIN_INACTIVE));

            verify(mockHttpFetcherService, times(1)).fetchFromUrl(any(String.class), anyInt());
        }
    }
}
