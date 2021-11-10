import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.Math.*;

public class satellite {
	static double SATELLITE_SPEED_METERS = 20200000;

	final static double PI = 2*java.lang.Math.acos(0.0);
	final static double TWOPI = 2.0*PI;

	static satellites satellitesClass;


	public static void main(String args[]) {
		satellitesClass = new satellites();

		double vehicleTime;
		double latitude; // in radians
		double longitude;
		double altitude;

		vehicleTime = Double.parseDouble(args[0]);
		latitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(args[1]),  Double.parseDouble(args[2]),  Double.parseDouble(args[3]),  Integer.parseInt(args[4]));
		longitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(args[5]),  Double.parseDouble(args[6]),  Double.parseDouble(args[7]),  Integer.parseInt(args[8]));
		altitude = Double.parseDouble(args[9]);

		//position of the vehicle at time t in cartesian coords
		Triplet<Double> cartCoords = cartCoordsUsingGeneralTime(latitudeLongitudeToCartesianCoords(latitude, longitude, altitude), vehicleTime);

		//output satellite positions and satellite times at vehicle time
		for(mySatellite sat: satellitePositions(vehicleTime)){
			System.out.println(sat.ID + " " + sat.time + " " + sat.satCartCoords.x1 + " " + sat.satCartCoords.x2 + " " + sat.satCartCoords.x3);
		}
	}

	//returns Satellite posit
	public static mySatellite[] satellitePositions(double vehicleTime){
		mySatellite[] everythingWeNeedAtVehicleTime = satellitesClass.getSatellites().clone();

		for (mySatellite mySatellite : everythingWeNeedAtVehicleTime) {
			Four_Tuple<Double> newValues = satellitePosition(mySatellite, vehicleTime);

			mySatellite.satCartCoords.x1 = newValues.x1;
			mySatellite.satCartCoords.x2 = newValues.x2;
			mySatellite.satCartCoords.x3 = newValues.x3;
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
				(double) (satellitesClass.givenRadiusOfPlanet + altitude) * cos(latitude) * cos(longitude),
				(double) (satellitesClass.givenRadiusOfPlanet + altitude) * cos(latitude) * sin(longitude),
				(double) (satellitesClass.givenRadiusOfPlanet + altitude) * sin(latitude));
		return position;
	}

	//Excercise 4: converts position in lat and long for general time t into cartesian coordinates.
	public static Triplet<Double> cartCoordsUsingGeneralTime(Triplet<Double> cartCoords, double time){
		double angle = (TWOPI * time)/satellitesClass.givenSiderealDay;

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

//Class that handles singleton for satellite array and the individual satellite data
//handles initial construction of satellite array.
class satellites {
	public double givenPi;
	public double givenSpeedOfLight;
	public double givenRadiusOfPlanet;
	public double givenSiderealDay;

	public final String dataFile = "data.dat";
	public final int numOfSatellites = 24;
	public final double degOfOrbitPlane = 55;
	public final int getNumOfSatellitesOnEachOrbit = 4;

	private mySatellite[] allSatellites;

	public satellites(){
		initSatellites();
	}

	public mySatellite[] getSatellites(){
		return this.allSatellites;
	}

	public void initSatellites(){
		if(allSatellites == null || allSatellites.length == 0){
			 this.allSatellites = new mySatellite[24];
			try{
				readDataFile();
			}
			catch(IOException e){
				System.out.println("Cannot read: " + dataFile);
			}
		}
		else
			return;
	}

	private void readDataFile() throws IOException {
		try(BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
			int index = 1;
			String line = br.readLine();

			while (line != null) {
				String lineWithoutComments = line.split("/=")[0];

				if(index == 1){
					givenPi = Double.parseDouble(lineWithoutComments);
				}
				else if(index == 2){
					givenSpeedOfLight = Double.parseDouble(lineWithoutComments);
				}
				else if(index == 3){
					givenRadiusOfPlanet = Double.parseDouble(lineWithoutComments);
				}
				else if(index == 4){
					givenSiderealDay = Double.parseDouble(lineWithoutComments);
				}
				else {
					int linesOfInfo = 9;
					int numOfSatSoFar = 0;

					while(line != null){
						ArrayList<Double> satInfo = new ArrayList<>();
						int endIndex = (index - 1) + linesOfInfo;
						lineWithoutComments = line.split("/=")[0];

						while(index != endIndex + 1){

							satInfo.add(Double.parseDouble(lineWithoutComments));

							line = br.readLine();
							index++;
						}

						this.allSatellites[numOfSatSoFar] = new mySatellite(numOfSatSoFar,
								satInfo.get(0),
								satInfo.get(1),
								satInfo.get(2),
								satInfo.get(3),
								satInfo.get(4),
								satInfo.get(5),
								satInfo.get(6),
								satInfo.get(7),
								satInfo.get(8));

						numOfSatSoFar++;
						index = endIndex;
					}
					return;
				}

				line = br.readLine();
				index++;
			}
		}
	}
}

class mySatellite {
	public final int ID;
	private final double initialTime;

	public double time;
	public Tuple<Double> satInitialLatLong;
	public Triplet<Double> satCartCoords;
	public Triplet<Double> vVector;
	public Triplet<Double> uVector;
	private double period;
	private double phase;
	private double altitude;

	public mySatellite(int ID, double time, double longitude, double latitude, double altitude){
		this.ID = ID;
		this.initialTime = time;
		this.satInitialLatLong = new Tuple<Double>(longitude, latitude);
		this.altitude = altitude;
	}

	public mySatellite(int ID, double u1, double u2, double u3, double v1, double v2, double v3, double periodicity, double altitude, double phase){
		this.ID = ID;
		this.initialTime = time;
		this.uVector = new Triplet<Double>(u1, u2, u3);
		this.vVector = new Triplet<Double>(v1, v2, v3);
		this.period = periodicity;
		this.altitude = altitude;
		this.phase = phase;
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
