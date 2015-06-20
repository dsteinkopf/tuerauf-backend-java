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
        final double dist = getDistanceFromHome(geoy, geox);
        return dist <= maxDist;
    }

    /**
     * Checks if coordinates are within maxDistOuter to home.
     */
    public boolean isNearToHomeOuter(final double geoy, final double geox) {
        final double dist = getDistanceFromHome(geoy, geox);
        return dist <= maxDistOuter;
    }

    /**
     * calculate distance between two geo coordinates.
     *
     * @return Distance in meters.
     */
    public double getDistance(final double lat1, final double lon1, final double lat2, final double lon2) {

        final double theta = lon1 - lon2;
        final double distCos = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        final double distAngleRad = Math.acos(distCos);
        final double distDeg = Math.toDegrees(distAngleRad);
        final double miles = distDeg * 60 * 1.1515;
        double km = miles * 1.609344;

        return km * 1000.0;
    }

    /**
     * Calculate distance from home.
     *
     * @return distance in meters.
     */
    public double getDistanceFromHome(final double geoy, final double geox) {
        return getDistance(geoy, geox, homeGeoy, homeGeox);
    }

    /**
     * calculate the angle (bearing) between two points.
     *
     * @return angle from point 1 to point 2. 0 = North, 90 = East, 180 = South, 270 = West.
     */
    public double getAngleFromCoordinate(double lat1Deg, double lon1Deg,
                                         double lat2Deg, double lon2Deg) {

        final double lon1 = Math.toRadians(lon1Deg);
        final double lat1 = Math.toRadians(lat1Deg);
        final double lon2 = Math.toRadians(lon2Deg);
        final double lat2 = Math.toRadians(lat2Deg);

        final double dLon = (lon2 - lon1);

        final double y = Math.sin(dLon) * Math.cos(lat2);
        final double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        final double bearingRad = Math.atan2(y, x);

        double bearingDeg = Math.toDegrees(bearingRad);
        bearingDeg = (bearingDeg + 360) % 360;
        // bearingDeg = 360 - bearingDeg;

        return bearingDeg;
    }

    /**
     * Calculate the direction (angle) from home to
     *
     * @return Direction from home. 0 = North, 90 = East, 180 = South, 270 = West.
     */
    public double getDirectionFromHome(double geoy, double geox) {
        return getAngleFromCoordinate(homeGeoy, homeGeox, geoy, geox);
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
