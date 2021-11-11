package src;

//Imports:

import java.lang.reflect.Array;

import static java.lang.Math.*;

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

    //Excercise 3: converts latitude and longitude position at time t = 0 into cartesian coordinates.
    public static Triplet<Double> latitudeLongitudeToCartesianCoords(double latitude, double longitude, double altitude){
        Triplet<Double> position
                = new Triplet<Double>(
                (double) (EARTH_RADIUS_METERS + altitude) * cos(latitude) * cos(longitude),
                (double) (EARTH_RADIUS_METERS + altitude) * cos(latitude) * sin(longitude),
                (double) (EARTH_RADIUS_METERS + altitude) * sin(latitude));
        return position;
    }

    public static Double degMinSecToLatitudeOrLongitude(double deg, double min, double sec, int NS){
        Double latOrLon = (double) (TWOPI) * ((deg/360) + (min/(360 *60) + sec/(360 * 60 * 60)));
        if(NS > 0){
            return latOrLon;
        }
        else{
            return -latOrLon;
        }
    }

    //Excercise 4: converts position in lat and long for general time t into cartesian coordinates.
    public static Triplet<Double> positionToLatAndLong(Triplet<Double> cartCoords, double time){
        double angle = (TWOPI * time)/SIDEREAL_DAY_SECONDS;

        return new Triplet<Double>((cartCoords.x1 * cos(angle)) + (cartCoords.x1 * sin(angle)),
                            (cartCoords.x2 * -sin(angle)) + (cartCoords.x2 * cos(angle)),
                            cartCoords.x3);
    }

    //Excercise 5: convert cartesian coords for t = 0 to latitude, longitude, and height
    //TODO: Guard against division by 0
    //TODO: longitude is between plus and minus pi. atan is between -pi/2 and pi/2
    public static Triplet<Double> cartCoordsToLatLongHeight(double x, double y, double z){
        double longitude = 0;
        double latitude = 0;
        double height = 0;
        double[] xyVector = {x, y};
        double[] xyzVector = {x, y, z};

        //Check conditions to find latitude
        if(twoNorm(xyVector) != 0){
            latitude = atan((double)z/twoNorm(xyVector));
        }

        else if(x == 0 && y == 0 && z > 0){
            latitude = PI/2;
        }

        else if(x == 0 && y == 0 && z < 0){
            latitude = -(PI/2);
        }

        //Check conditions to find longitude
        if(x > 0 && y > 0){
            longitude = atan((double)y/(double)x);
        }
        else if(x < 0){
            longitude = PI + atan((double)y/(double)x);
        }
        else if(x > 0 && y < 0){
            longitude = TWOPI + atan((double)y/(double)x);
        }

        //Find height
        height = twoNorm(xyzVector) - EARTH_RADIUS_METERS;

        //return new 3-vector with latitude, longitude, and height
        return new Triplet<Double>(latitude, longitude, height);
    }

    //Exercise 6: converts general time t and a position given in cartesian coordinates into latitude and longitude
    //public static Triplet<Double>

    //Exercise 7: Test previous formulas
    public static void lampostTest(){
        //Should be 0.7114883177
        System.out.println(degMinSecToLatitudeOrLongitude(40, 45, 55, 1));
        //Should be -1.9521410721
        System.out.println(degMinSecToLatitudeOrLongitude(111, 50, 58, -1));

        Triplet<Double> cartCoords = latitudeLongitudeToCartesianCoords(degMinSecToLatitudeOrLongitude(40, 45, 55, 1), degMinSecToLatitudeOrLongitude(111, 50, 58, -1), 1372.0);
        System.out.println(cartCoords.x1 + " " + cartCoords.x2+ " " + cartCoords.x3);
    }

    //calculates the 2-norm for a given double vector
    public static double twoNorm(double[] vector){
        double sqrAndSum = 0;
        for(double element: vector){
            sqrAndSum += (element * element);
        }
        return sqrAndSum;
    }
}

