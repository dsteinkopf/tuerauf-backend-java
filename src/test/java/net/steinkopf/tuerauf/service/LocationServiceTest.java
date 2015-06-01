package net.steinkopf.tuerauf.service;

import net.steinkopf.tuerauf.TueraufApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


/**
 * Tests for LocationService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TueraufApplication.class)
public class LocationServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceTest.class);

    /**
     * System under Test.
     */
    @Autowired
    LocationService locationService;


    @Before
    public void setUp() throws Exception {

        locationService.setHomeGeoy(8.109535);
        locationService.setHomeGeox(1.622306);
        locationService.setMaxDist(40.0);
        locationService.setMaxDistOuter(80.0);
    }

    @Test
    public void testIsNearToHome() throws Exception {

        assertThat(locationService.isNearToHome(8.109388, 1.622288), is(equalTo(true)));
        assertThat(locationService.isNearToHome(8.109388, 1.622888), is(equalTo(false)));
    }

    @Test
    public void testIsNearToHomeOuter() throws Exception {

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
}
