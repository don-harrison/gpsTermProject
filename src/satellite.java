import java.lang.reflect.Array;

import static java.lang.Math.*;
import java.lang.Scanner;

class mySatellite
{
	private int ID;
	private double time;
	private double latitude; // in radians
	private double longitude;
	private double period;
	private double altitude;
	private double phase;
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

	private mySatellite[] satellites = new mySatellite[24];

	public static void main(String args[])
	{
		double time;
		double latitude; // in radians
		double longitude;
		double altitude;

		time = Double.parseDouble(args[0]);
		latitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(args[1]),  Double.parseDouble(args[2]),  Double.parseDouble(args[3]),  Double.parseDouble(args[4]));
		longitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(args[5]),  Double.parseDouble(args[6]),  Double.parseDouble(args[7]),  Double.parseDouble(args[8]));
		altitude = Double.parseDouble(args[9]);

		//position of the vehicle at time t in cartesian coords
		Triplet<Double> catCoords = cartCoordsUsingGeneralTime(latitudeLongitudeToCartesianCoords(latitude, longitude, altitude), time);
		System.out.println(catCoords);
		//TODO: send to the receiver
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
	/*
	private void readVehicle()
	{
		Scanner scanner = new Scanner(inputStream);
		time = Double.parse(scanner.next());
		int deg = Integer.parse(scanner.next());
		int min = Integer.parse(scanner.next());
		double sec = Double.parse(scanner.next());
		north = Integer.parse(scanner.next()) == 1;
		latitude = angles.rad(deg, min, sec, north);
		deg = Integer.parse(scanner.next());
		min = Integer.parse(scanner.next());
		sec = Double.parse(scanner.next());
		east = Integer.parse(scanner.next()) == 1;
		longitude = angles.rad(deg, min, sec, east);
		altitude = Double.parse(scanner.next());
	}*/

	/**
	* returns its own ID, when to send the signal, and where it will be when it sends it, in that order
	*/
	private double[] timeToSend()
	{
		
		double epsilon = 0.00000000001;
		while (change < epsilon)
		{
			return null;
		}
	}

	private void readData(File file)
	{
		// TODO: ignore comments in file
		Scanner scanner = new Scanner(file);
		pi = scanner.nextLine();
		c = scanner.nextLine();
		r = scanner.nextLine();
		s = scanner.nextLine();
		for (int i = 0; i < 24; i++)
		{
			int deg = scanner.nextLine();
			int min = scanner.nextLine();
			double sec = scanner.nextLine();
		}
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
