//Imports:
import model.Triplet;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class assortedFunctions {
    //class variables
    static double EARTH_RADIUS_METERS = 6367444.50;

    static double SIDEREAL_DAY_SECONDS = 86164.09;

    static double SATELLITE_SPEED_METERS = 20200000;

    final static double PI = 2*java.lang.Math.acos(0.0);
    final static double TWOPI = 2.0*PI;

    //Excercise 1: Formula that describes the trajectory of the point O in cartesian coordinates as a function of time
    public static Triplet<Double> stationaryObjectTrajectory(double time){
        Triplet<Double> position
                = new Triplet<Double>(
                                        (double) EARTH_RADIUS_METERS * cos((TWOPI*time)/SIDEREAL_DAY_SECONDS),
                                        (double) EARTH_RADIUS_METERS * sin((TWOPI*time)/SIDEREAL_DAY_SECONDS),
                                        (double) 0);
        return position;
    }
}
