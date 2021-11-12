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
		ArrayList<String> allArgs = getArgs();
		satellitesClass = new satellites();

		for(String argLine: allArgs){
			String[] listOfArgs = argLine.split(" ");

			double vehicleTime;
			double latitude; // in radians
			double longitude;
			double altitude;

			vehicleTime = Double.parseDouble(listOfArgs[0]);
			latitude = angles.rad( Integer.parseInt(listOfArgs[1]),
					Integer.parseInt(listOfArgs[2]),  Double.parseDouble(listOfArgs[3]),  Integer.parseInt(listOfArgs[4]));
			longitude = angles.rad( Integer.parseInt(listOfArgs[5]),
					Integer.parseInt(listOfArgs[6]),  Double.parseDouble(listOfArgs[7]),  Integer.parseInt(listOfArgs[8]));
			altitude = Double.parseDouble(listOfArgs[9]);
			//position of the vehicle at time t in cartesian coords
			Triplet cartCoords = cartCoordsUsingGeneralTime(latitudeLongitudeToCartesianCoords(latitude, longitude, altitude), vehicleTime);

			//vehicle to carry vehicle data that we calculated above
			myVehicle vehicle = new myVehicle(vehicleTime, longitude, latitude, altitude, cartCoords);

			for(int j = 0; j < 24; j++) {
				mySatellite currSatellite = satellitesClass.getSatellites()[j];
				currSatellite.sendTime = satelliteTimeNewton(vehicle, currSatellite);
				currSatellite.sendPos = satellitePositionAtTime(currSatellite, currSatellite.sendTime);

				// check if current satellite above horizon
				if(checkAboveHorizon(currSatellite.sendPos, vehicle.cartCoords))
				{
					writeToLogFile(currSatellite.ID + " " + currSatellite.sendTime + " " + currSatellite.sendPos.x1 + " " + currSatellite.sendPos.x2 + " " + currSatellite.sendPos.x3 , "satellite output");
					System.out.println(currSatellite.ID + " " + currSatellite.sendTime + " " + currSatellite.sendPos.x1 + " " + currSatellite.sendPos.x2 + " " + currSatellite.sendPos.x3 );
				}
			}
		}
	}
	
	private static boolean checkAboveHorizon(Triplet positionSatellite, Triplet positionVehicle) {
		double satNorm = twoNorm(positionSatellite);
        double vNorm = twoNorm(positionVehicle);
        Triplet diff = new Triplet(0.1,0.1,0.1);
        diff.x1 = positionSatellite.x1 - positionVehicle.x1;
        diff.x2 = positionSatellite.x2 - positionVehicle.x2;
        diff.x3 = positionSatellite.x3 - positionVehicle.x3;
        double diffNorm = twoNorm(diff);

        if(diffNorm < twoNorm(new double[]{ satNorm, vNorm }))
        {
            return true;
        }
        return false;
	}

	private static ArrayList<String> getArgs(){
		ArrayList<String> listOfArgs = new ArrayList<String>();
		//How we handle piping file contents in as args
		try{
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			String x;
			while( (x = input.readLine()) != null ) {
				writeToLogFile(x, "satellite input");
				listOfArgs.add(x);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listOfArgs;
	}
	
	//FIgure 36: x_s(t) = ...
	// returns the position of satellite sat at time time
	private static Triplet satellitePositionAtTime(mySatellite sat, double time){
		return
				new Triplet((satellitesClass.givenRadiusOfPlanet + sat.altitude) * ((sat.uVectorInCartesian.x1 * cos(((TWOPI * time)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x1 * sin(((TWOPI * time)/ sat.period) + sat.phase))),
						(satellitesClass.givenRadiusOfPlanet + sat.altitude) * ((sat.uVectorInCartesian.x2 * cos(((TWOPI * time)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x2 * sin(((TWOPI * time)/ sat.period) + sat.phase))),
						(satellitesClass.givenRadiusOfPlanet + sat.altitude) * ((sat.uVectorInCartesian.x3 * cos(((TWOPI * time)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x3 * sin(((TWOPI * time)/ sat.period) + sat.phase))));
	}

	// given vehicle time and position in cartesian, returns newton's method for time to send specific satellite info
	private static double satelliteTimeNewton(myVehicle vehicle, mySatellite sat)
	{
		Triplet sPos = satellitePositionAtTime(sat, vehicle.time);
		//start lastTime at t0
		//TODO: why does this two norm work without taking a square root
		double lastTime = vehicle.time - (twoNorm(new double[]{sPos.x1 - vehicle.cartCoords.x1, sPos.x2 - vehicle.cartCoords.x2, sPos.x3 - vehicle.cartCoords.x3})/satellitesClass.givenSpeedOfLight);
		double nextTime = 0;
		int timesRan = 0;
		while(!(Math.abs(nextTime - lastTime) < (0.01/satellitesClass.givenSpeedOfLight))) {
			double nextTimeCopy = nextTime;

			nextTime = lastTime - functionToBeSolvedUsingNewtonsMethod(vehicle, sat, lastTime) /
					derivativeOfFunctionToBeSolvedUsingNewtonsMethod(vehicle, sat, lastTime);

			lastTime = nextTimeCopy;
			timesRan++;
		}
		return nextTime;
	}
	
	//Figure 37/38:
	private static double functionToBeSolvedUsingNewtonsMethod(myVehicle vehicle, mySatellite sat, double sTime){
		return Math.pow(satellitePositionAtTime(sat, sTime).x1 - vehicle.cartCoords.x1, 2) +
				Math.pow(satellitePositionAtTime(sat, sTime).x2 - vehicle.cartCoords.x2, 2) +
				Math.pow(satellitePositionAtTime(sat, sTime).x3 - vehicle.cartCoords.x3, 2) -
				Math.pow(satellitesClass.givenSpeedOfLight, 2) * Math.pow(vehicle.time - sTime, 2);
	}

	//Figure 40: Derivative of the function that returns satelliteTime
	private static double derivativeOfFunctionToBeSolvedUsingNewtonsMethod(myVehicle vehicle, mySatellite sat, double sTime){
		double part1 = ((4 * satellitesClass.givenPi) * (satellitesClass.givenRadiusOfPlanet + sat.altitude)/sat.period);
		Triplet satPosition = satellitePositionAtTime(sat, sTime);
		Triplet part2FirstVectorTranspose
				= new Triplet(satPosition.x1 - vehicle.cartCoords.x1,
				satPosition.x2 - vehicle.cartCoords.x2,
				satPosition.x3 - vehicle.cartCoords.x3
		);

		Triplet part2SecondVector = new Triplet(((-sat.uVectorInCartesian.x1 * sin(((TWOPI * sTime)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x1 * cos(((TWOPI * sTime)/ sat.period) + sat.phase))),
				((-sat.uVectorInCartesian.x2 * sin(((TWOPI * sTime)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x2 * cos(((TWOPI * sTime)/ sat.period) + sat.phase))),
				((-sat.uVectorInCartesian.x3 * sin(((TWOPI * sTime)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x3 * cos(((TWOPI * sTime)/ sat.period) + sat.phase))));

		double part2Double = (part2FirstVectorTranspose.x1 * part2SecondVector.x1) + (part2FirstVectorTranspose.x2 * part2SecondVector.x2) + (part2FirstVectorTranspose.x3 + part2SecondVector.x3);

		double part3 = 2 * Math.pow(satellitesClass.givenSpeedOfLight, 2) * (vehicle.time - sTime);

		return part1 * part2Double + part3;
	}

	//Excercise 3: converts latitude and longitude position at time t = 0 into cartesian coordinates.
	//CHECKED
	public static Triplet latitudeLongitudeToCartesianCoords(double latitude, double longitude, double altitude){
		Triplet position
				= new Triplet(
				(double) (satellitesClass.givenRadiusOfPlanet + altitude) * cos(latitude) * cos(longitude),
				(double) (satellitesClass.givenRadiusOfPlanet + altitude) * cos(latitude) * sin(longitude),
				(double) (satellitesClass.givenRadiusOfPlanet + altitude) * sin(latitude));
		return position;
	}

	//Excercise 4: converts position in lat and long for general time t into cartesian coordinates.
	//CHECKED
	public static Triplet cartCoordsUsingGeneralTime(Triplet cartCoords, double time){
		double angle = (TWOPI * time)/satellitesClass.givenSiderealDay;

		return new Triplet(
				(cos(angle) * cartCoords.x1) - (sin(angle) * cartCoords.x2),
				(sin(angle) * cartCoords.x1) + (cos(angle) * cartCoords.x2),
				cartCoords.x3);
	}

	//Writes the log of standard input and output
	//CHECKED
	private static void writeToLogFile(String arg, String comment){
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
			writer.append(arg + " //" + comment + "\n");

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
		return sqrt(sqrAndSum);
	}

	public static double twoNorm(Triplet vector){
		double sqrAndSum = 0;
		sqrAndSum += vector.x1 * vector.x1;
		sqrAndSum += vector.x2 * vector.x2;
		sqrAndSum += vector.x3 * vector.x3;
		return sqrt(sqrAndSum);
	}
}

//Class that handles singleton for satellite array and the individual satellite data
//handles initial construction of satellite array.
//CHECKED
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

	//Reads from data.dat in the same directory as satellites. Writes data into satellite array.
	//CHECKED
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

						while(index != endIndex + 1){
							lineWithoutComments = line.split("/=")[0];
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
	// for returning
	public double sendTime;
	public Triplet sendPos; // in cartesian

	// for reading
	public Triplet uVectorInCartesian;
	public Triplet vVectorInCartesian;
	public double period;
	public double phase;
	public double altitude;

	//constructor for latLong
	public mySatellite(int ID, double u1, double u2, double u3, double v1, double v2, double v3, double periodicity, double altitude, double phase){
		this.ID = ID;
		this.uVectorInCartesian = new Triplet(u1, u2, u3);
		this.vVectorInCartesian = new Triplet(v1, v2, v3);
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
	public Triplet cartCoords;

	public myVehicle(double time, double longitude, double lat, double alt, Triplet cartCoords){
		this.time = time;
		this.longitude = longitude;
		this.lat = lat;
		this.alt = alt;
		this.cartCoords = new Triplet(cartCoords.x1, cartCoords.x2, cartCoords.x3);
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

class Triplet {
	public double x1;
	public double x2;
	public double x3;

	public Triplet(double x1, double x2, double x3){
		this.x1 = x1;
		this.x2 = x2;
		this.x3 = x3;
	}
	
	public Triplet minus(Triplet other)
	{
		return new Triplet( (this.x1) - (other.x1),
				 (this.x2) - (other.x2),
				 (this.x3) - (other.x3));
	}
}
