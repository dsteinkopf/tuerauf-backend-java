package net.steinkopf.tuerauf.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Location calculations.
 */
@Service
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);


    @Value("${tuerauf.home.geoy}")
    private double homeGeoy;

    @Value("${tuerauf.home.geox}")
    private double homeGeox;

    @Value("${tuerauf.home.max-dist-outer}")
    private double maxDistOuter; // in meters

    @Value("${tuerauf.home.max-dist}")
    private double maxDist; // in meters


    /**
     * Checks if coordinates are within maxDist to home.
     */
    public boolean isNearToHome(final double geoy, final double geox) {
        final double dist = getDistance(geoy, geox, homeGeoy, homeGeox);
        logger.debug("isNearToHome: geoy={} geox={} dist={}m");
        return dist <= maxDist;
    }

    /**
     * Checks if coordinates are within maxDistOuter to home.
     */
    public boolean isNearToHomeOuter(final double geoy, final double geox) {
        final double dist = getDistance(geoy, geox, homeGeoy, homeGeox);
        logger.debug("isNearToHomeOuter: geoy={} geox={} dist={}m");
        return dist <= maxDistOuter;
    }

    /**
     * calculate distance between two geo coordinates.
     * @return Distance in meters.
     */
    public double getDistance(final double lat1, final double lon1, final double lat2, final double lon2) {

        final double theta = lon1 - lon2;
        final double distCos = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                +  Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        final double distAngleRad = Math.acos(distCos);
        final double distDeg = Math.toDegrees(distAngleRad);
        final double miles = distDeg * 60 * 1.1515;
        double km = miles * 1.609344;

        return km * 1000.0;
    }

    void setHomeGeoy(final double homeGeoy) {
        this.homeGeoy = homeGeoy;
    }

    void setHomeGeox(final double homeGeox) {
        this.homeGeox = homeGeox;
    }

    void setMaxDistOuter(final double maxDistOuter) {
        this.maxDistOuter = maxDistOuter;
    }

    void setMaxDist(final double maxDist) {
        this.maxDist = maxDist;
    }
}
