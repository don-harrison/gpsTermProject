import java.io.File;
import java.lang.reflect.Array;

import static java.lang.Math.*;

//Class that handles singleton for satellite array and the individual satellite data
//handles initial construction of satellite array.
class satellites {
	public final int numOfSatellites = 24;
	public final double degOfOrbitPlane = 55;
	public final int getNumOfSatellitesOnEachOrbit = 4;

	private mySatellite[] allSatellites;

	public satellites(){
		if(allSatellites == null || allSatellites.length == 0){
			//call Init satellites to constuct array of satellites;
		}
		else
			return;
	}

	public mySatellite[] getSatellites(){
		return this.allSatellites;
	}

	//TODO: initialize the satellites
	private void initSatellites(){

	}
}

class mySatellite {
	public final int ID;
	private final double initialTime;
	private final double initialLatitude; //in radians
	private final double initialLongitude; //in radians
	private final double initialAltitude;

	public double time;
	public double x; //in radians
	public double y; //in radians
	public double z;

	private double period;
	private double phase;

	public mySatellite(int ID, double time, double latitude, double longitude, double altitude){
		this.ID = ID;
		this.initialTime = time;
		this.initialLatitude = latitude;
		this.initialLongitude = longitude;
		this.initialAltitude = altitude;
	}
}

public class satellite {
	private double pi;
	private double c;
	private double r;
	private double s;

	static double EARTH_RADIUS_METERS = 6367444.50;

	static double SIDEREAL_DAY_SECONDS = 86164.09;

	static double SATELLITE_SPEED_METERS = 20200000;

	final static double PI = 2*java.lang.Math.acos(0.0);
	final static double TWOPI = 2.0*PI;

	//creates satellite arrays with initial positions
	static satellites allSatellites;

	public static void main(String args[])
	{
		int[] ids = new int[10];
		allSatellites = new satellites();

		double vehicleTime;
		double latitude; // in radians
		double longitude;
		double altitude;

		vehicleTime = Double.parseDouble(args[0]);
		latitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(args[1]),  Double.parseDouble(args[2]),  Double.parseDouble(args[3]),  Integer.parseInt(args[4]));
		longitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(args[5]),  Double.parseDouble(args[6]),  Double.parseDouble(args[7]),  Integer.parseInt(args[8]));
		altitude = Double.parseDouble(args[9]);

		//position of the vehicle at time t in cartesian coords
		Triplet<Double> catCoords = cartCoordsUsingGeneralTime(latitudeLongitudeToCartesianCoords(latitude, longitude, altitude), vehicleTime);

		//output satellite positions and satellite times at vehicle time
		for(mySatellite sat: satellitePositions(vehicleTime)){
			System.out.println(sat.ID + " " + sat.time + " " + sat.x+ " " + sat.y + " " + sat.z);
		}
	}

	//returns Satellite posit
	public static mySatellite[] satellitePositions(double vehicleTime){
		mySatellite[] everythingWeNeedAtVehicleTime = allSatellites.getSatellites().clone();

		for (mySatellite mySatellite : everythingWeNeedAtVehicleTime) {
			Four_Tuple<Double> newValues = satellitePosition(mySatellite, vehicleTime);

			mySatellite.z = newValues.x1;
			mySatellite.x = newValues.x2;
			mySatellite.y = newValues.x3;
			mySatellite.time = newValues.x4;
		}

		return everythingWeNeedAtVehicleTime;
	}

	//x_s(t) = ...
	private static Four_Tuple<Double> satellitePosition(mySatellite sat, double vehicleTime){
		//TODO: DO EXERCISE 9 HERE:

		return new Four_Tuple<Double>(0.0, 0.0, 0.0, 0.0);
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

	//Excercise 4: converts position in lat and long for general time t into cartesian coordinates.
	public static Triplet<Double> cartCoordsUsingGeneralTime(Triplet<Double> cartCoords, double time){
		double angle = (TWOPI * time)/SIDEREAL_DAY_SECONDS;

		return new Triplet<Double>(
				(cos(angle) * cartCoords.x1) + (-sin(angle) * cartCoords.x2),
				(sin(angle) * cartCoords.x1) + (cos(angle) * cartCoords.x2),
				cartCoords.x3);
	}

	// helper for constructor
	private static Double degMinSecToLatitudeOrLongitude(double deg, double min, double sec, int NS){
		Double latOrLon = (double) (TWOPI) * ((deg/360) + (min/(360 *60) + sec/(360 * 60 * 60)));
		if(NS > 0){
			return latOrLon;
		}
		else{
			return -latOrLon;
		}
	}

	//TODO: write method to report logs
	private static void writeLogFile(mySatellite[] sats){

	}
}

class Four_Tuple<T> {
	public T x1;
	public T x2;
	public T x3;
	public T x4;

	public Four_Tuple(T x1, T x2, T x3, T x4){
		this.x1 = x1;
		this.x2 = x2;
		this.x3 = x3;
		this.x4 = x4;
	}
}

class Triplet<T> {
	public T x1;
	public T x2;
	public T x3;

	public Triplet(T x1, T x2, T x3){
		this.x1 = x1;
		this.x2 = x2;
		this.x3 = x3;
	}
}

class Tuple<T> {
	public T x1;
	public T x2;

	public Tuple(T x1, T  x2){
		this.x1 = x1;
		this.x2 = x2;
	}
}
