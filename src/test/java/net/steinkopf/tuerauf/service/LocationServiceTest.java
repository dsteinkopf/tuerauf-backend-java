package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.TueraufApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;


/**
 * Tests for LocationService
 */
@SpringApplicationConfiguration(classes = TueraufApplication.class)
@TestExecutionListeners(inheritListeners = false, listeners = {
        DependencyInjectionTestExecutionListener.class }) // see http://stackoverflow.com/questions/25537436/integration-testing-a-spring-boot-web-app-with-testng
public class LocationServiceTest extends AbstractTestNGSpringContextTests {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceTest.class);

    /**
     * System under Test.
     */
    @Autowired
    LocationService locationService;


    @BeforeClass
    public void setUp() throws Exception {

        locationService.setHomeGeoy(8.109535);
        locationService.setHomeGeox(1.622306);
        locationService.setMaxDist(40.0);
        locationService.setMaxDistOuter(80.0);
    }

    @Test
    public void testIsNearToHome() throws Exception {

        logger.debug("testIsNearToHome");
        assertThat(locationService.isNearToHome(8.109388, 1.622288), is(equalTo(true)));
        assertThat(locationService.isNearToHome(8.109388, 1.622888), is(equalTo(false)));
    }

    @Test
    public void testIsNearToHomeOuter() throws Exception {

        logger.debug("testIsNearToHomeOuter");
        assertThat(locationService.isNearToHomeOuter(8.109388, 1.622888), is(equalTo(true)));
        assertThat(locationService.isNearToHomeOuter(8.109388, 1.624288), is(equalTo(false)));
    }

    @Test
    public void testGetDistance() throws Exception {

        final double y_min = 8.109388;
        final double y_max = 8.109584;
        final double x_min = 1.622288;
        final double x_max = 1.622506;

        final double dist = locationService.getDistance(y_min, x_min, y_max, x_max);

        assertThat(dist, is(greaterThan(10.0)));
        assertThat(dist, is(lessThan(50.0)));
    }

    @Test
    public void testGetAngleFromCoordinate() throws Exception {

        assertThat(Math.round(locationService.getAngleFromCoordinate(0,0, 0,1)), is(equalTo(90L)));
        assertThat(Math.round(locationService.getAngleFromCoordinate(0,0, 1,0)), is(equalTo(0L)));
        assertThat(Math.round(locationService.getAngleFromCoordinate(0,0, 0,-1)), is(equalTo(270L)));
        assertThat(Math.round(locationService.getAngleFromCoordinate(0,0, 1,1)), is(equalTo(45L)));

        assertThat(Math.round(locationService.getAngleFromCoordinate(12,48, 12,49)), is(equalTo(90L)));
        assertThat(Math.round(locationService.getAngleFromCoordinate(12,48, 13,48)), is(equalTo(0L)));
        assertThat(Math.round(locationService.getAngleFromCoordinate(12,48, 12,47)), is(equalTo(270L)));
    }
}
