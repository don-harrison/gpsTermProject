import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Math.*;
 

//test change
public class satellite {
	static double SATELLITE_SPEED_METERS = 20200000;

	final static double PI = 2*java.lang.Math.acos(0.0);
	final static double TWOPI = 2.0*PI;

	static satellites satellitesClass;


	public static void main(String args[]) {
		ArrayList<String> listOfArgs = getArgs();

		satellitesClass = new satellites();

		double vehicleTime;
		double latitude; // in radians
		double longitude;
		double altitude;

		vehicleTime = Double.parseDouble(listOfArgs.get(0));
		latitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(listOfArgs.get(0)),  Double.parseDouble(listOfArgs.get(0)),  Double.parseDouble(listOfArgs.get(0)),  Integer.parseInt(listOfArgs.get(0)));
		longitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(listOfArgs.get(0)),  Double.parseDouble(listOfArgs.get(0)),  Double.parseDouble(listOfArgs.get(0)),  Integer.parseInt(listOfArgs.get(0)));
		altitude = Double.parseDouble(listOfArgs.get(0));
		//position of the vehicle at time t in cartesian coords
		Triplet<Double> cartCoords = cartCoordsUsingGeneralTime(latitudeLongitudeToCartesianCoords(latitude, longitude, altitude), vehicleTime);
		myVehicle vehicle = new myVehicle(vehicleTime, longitude, latitude, altitude, cartCoords);

		//output satellite positions and satellite times at vehicle time
		//TODO: WHAT SATELLITE GOES HERE? :,(
//		for(mySatellite sat: satellitePositions(vehicle, )){
//			System.out.println(sat.ID + " " + sat.time + " " + sat.satCartCoords.x1 + " " + sat.satCartCoords.x2 + " " + sat.satCartCoords.x3);
//		}
	}

	private static ArrayList<String> getArgs(){
		ArrayList<String> listOfArgs = new ArrayList<String>();
		//How we handle piping file contents in as args
		try{
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			String x;
			while( (x = input.readLine()) != null ) {
				writeToLogFile(x);
				listOfArgs.add(x);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listOfArgs;
	}

	//returns Satellite positions
	public static mySatellite[] satellitePositions(myVehicle vehicle, mySatellite sat, double time){
		mySatellite[] everythingWeNeedAtVehicleTime = satellitesClass.getSatellites().clone();

		for (mySatellite mySatellite : everythingWeNeedAtVehicleTime) {
			//TODO: finish this once Exercise 9 stuff is done
			Triplet<Double> satellitePos = satellitePosition(mySatellite, vehicle.time);

			mySatellite.satCartCoords.x1 = satellitePos.x1;
			mySatellite.satCartCoords.x2 = satellitePos.x2;
			mySatellite.satCartCoords.x3 = satellitePos.x3;
			mySatellite.time = getSatelliteTimeUsingVehicleTime(vehicle, sat, time);
		}

		return everythingWeNeedAtVehicleTime;
	}

	//FIgure 36: x_s(t) = ...
	private static Triplet<Double> satellitePosition(mySatellite sat, double vehicleTime){
		Triplet<Double> uPosAdjusted =
				new Triplet<>((satellitesClass.givenRadiusOfPlanet * sat.altitude) * ((sat.uVector.x1 * cos(((TWOPI * vehicleTime)/ satellitesClass.givenSiderealDay) + sat.period)) + (sat.vVector.x1 * sin(((TWOPI * vehicleTime)/ satellitesClass.givenSiderealDay) + sat.period))),
						(satellitesClass.givenRadiusOfPlanet * sat.altitude) * ((sat.uVector.x2 * cos(((TWOPI * vehicleTime)/ satellitesClass.givenSiderealDay) + sat.period)) + (sat.vVector.x2 * sin(((TWOPI * vehicleTime)/ satellitesClass.givenSiderealDay) + sat.period))),
						(satellitesClass.givenRadiusOfPlanet * sat.altitude) * ((sat.uVector.x3 * cos(((TWOPI * vehicleTime)/ satellitesClass.givenSiderealDay) + sat.period)) + (sat.vVector.x3 * sin(((TWOPI * vehicleTime)/ satellitesClass.givenSiderealDay) + sat.period))));
		return new Triplet<Double>(uPosAdjusted.x1, uPosAdjusted.x2, uPosAdjusted.x3);
	}
	//Figure 37/38:
	private static double functionToBeSolvedUsingNewtonsMethod(myVehicle vehicle, mySatellite sat, double time){
		return twoNorm(new double[]{satellitePosition(sat, time).x1 - vehicle.cartCoords.x1,
									satellitePosition(sat, time).x2 - vehicle.cartCoords.x2,
									satellitePosition(sat, time).x3 - vehicle.cartCoords.x3})
				- (Math.pow(satellitesClass.givenSpeedOfLight, 2) * Math.pow(vehicle.time - time, 2));
	}

	//Figure 40: Derivative of the function that returns satelliteTime
	private static double derivativeOfFunctionToBeSolvedUsingNewtonsMethod(mySatellite sat, myVehicle vehicle, double time){
		double part1 = ((4 * satellitesClass.givenPi) * (satellitesClass.givenRadiusOfPlanet + sat.altitude)/satellitesClass.givenSiderealDay);
		Triplet<Double> satPosition = satellitePosition(sat, time);
		Triplet<Double> part2FirstVectorTranspose
				= new Triplet<>(satPosition.x1 - vehicle.cartCoords.x1,
				satPosition.x2 - vehicle.cartCoords.x2,
				satPosition.x3 - vehicle.cartCoords.x3
		);

		Triplet<Double> part2SecondVector = new Triplet<>(((-sat.uVector.x1 * sin(((TWOPI * time)/ satellitesClass.givenSiderealDay) + sat.period)) + (sat.vVector.x1 * cos(((TWOPI * time)/ satellitesClass.givenSiderealDay) + sat.period))),
				((-sat.uVector.x2 * sin(((TWOPI * time)/ satellitesClass.givenSiderealDay) + sat.period)) + (sat.vVector.x2 * cos(((TWOPI * time)/ satellitesClass.givenSiderealDay) + sat.period))),
				((-sat.uVector.x3 * sin(((TWOPI * time)/ satellitesClass.givenSiderealDay) + sat.period)) + (sat.vVector.x3 * cos(((TWOPI * time)/ satellitesClass.givenSiderealDay) + sat.period))));

		double part2Double = (part2FirstVectorTranspose.x1 * part2SecondVector.x1) + (part2FirstVectorTranspose.x2 * part2SecondVector.x2) + (part2FirstVectorTranspose.x3 + part2SecondVector.x3);

		double part3 = 2 * Math.pow(satellitesClass.givenSpeedOfLight, 2) * (vehicle.time - time);

		return part1 * part2Double * part3;
	}

	//Figure 39: Find the satellite time at some vehicleTime
	private static double getSatelliteTimeUsingVehicleTime(myVehicle vehicle, mySatellite sat, double time){
		double satTime1 = time;
		double satTime2 = 0.0;
		double convergenceThreshold = .01/satellitesClass.givenSpeedOfLight;
		int iterationTracker = 0;

		try{
			while(!((satTime1 - satTime2) < convergenceThreshold)){
				if(iterationTracker > 20){
					throw new Exception("getSatelliteTimeUsingVehicleTime is taking too long to iterate. god is dead");
				}
				//Do Newtons method on f(x)
				satTime2 = satTime1 - (functionToBeSolvedUsingNewtonsMethod(vehicle, sat, time)/derivativeOfFunctionToBeSolvedUsingNewtonsMethod(sat, vehicle, time));
				iterationTracker++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		return satTime2;
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

	//Writes the log of standard input and output
	private static void writeToLogFile(String arg){
		File satelliteLog = new File("satellite.log");
		//Write stuff here
		if(!(new File("satellite.log").exists())){
			try{
				satelliteLog.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("satellite.log", true));
			writer.append(arg + "\n");

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public mySatellite[] getSatellites(){
		return this.allSatellites;
	}


	private void readDataFile() throws IOException {
		try(BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
			String line = br.readLine();
			
			// only 1 copy of this info
			for(int i = 1; i < 5; i++) {
				String lineWithoutComments = line.split("/=")[0];
	
				if(i == 1){
					givenPi = Double.parseDouble(lineWithoutComments);
				}
				else if(i == 2){
					givenSpeedOfLight = Double.parseDouble(lineWithoutComments);
				}
				else if(i == 3){
					givenRadiusOfPlanet = Double.parseDouble(lineWithoutComments);
				}
				else if(i == 4){
					givenSiderealDay = Double.parseDouble(lineWithoutComments);
				}
				// on last run, reads the 5th line (u1 of sat 0)
				line = br.readLine();
			}
			int numOfSatSoFar = 0;
			// this while loop should go once per satellite
			while(line != null){
				// store each satellite's info
				ArrayList<Double> satInfo = new ArrayList<>();
				String lineWithoutComments = line.split("/=")[0];

				// read in each satellite's data all at once
				for(int i = 0; i < 9; i++){
					satInfo.add(Double.parseDouble(lineWithoutComments));
					line = br.readLine();
					lineWithoutComments = line.split("/=")[0];
				}

				// runs once for each satellite
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
			}
		}
	}
}

class mySatellite {
	public final int ID;
	public double initialTime;

	public double time;
	public Tuple<Double> satInitialLatLong;
	public Triplet<Double> satCartCoords;
	public Triplet<Double> vVector;
	public Triplet<Double> uVector;
	public double period;
	public double phase;
	public double altitude;

	//constructor for long lat altitude
	public mySatellite(int ID, double time, double longitude, double latitude, double altitude){
		this.ID = ID;
		this.initialTime = time;
		this.satInitialLatLong = new Tuple<Double>(longitude, latitude);
		this.altitude = altitude;
		//TODO: add phase, period info
	}

	//constructor for cartesian
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


// for testing?
class myVehicle{
	public double lat;
	public double longitude;
	public double alt;
	public double time;
	public Triplet<Double> cartCoords;

	public myVehicle(double time, double longitude, double lat, double alt, Triplet<Double> cartCoords){
		this.time = time;
		this.longitude = longitude;
		this.lat = lat;
		this.alt = alt;
		this.cartCoords = new Triplet<>(cartCoords.x1, cartCoords.x2, cartCoords.x3);
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
