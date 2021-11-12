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

		for(int i = 0; i < listOfArgs.size()/10; i++)
		{
			int vIter = i * 10;
			vehicleTime = Double.parseDouble(listOfArgs.get(vIter));
			latitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(listOfArgs.get(vIter + 1)),  
					Double.parseDouble(listOfArgs.get(vIter+2)),  Double.parseDouble(listOfArgs.get(vIter+3)),  Integer.parseInt(listOfArgs.get(vIter+4)));
			longitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(listOfArgs.get(vIter+5)),  
					Double.parseDouble(listOfArgs.get(vIter+6)),  Double.parseDouble(listOfArgs.get(vIter+7)),  Integer.parseInt(listOfArgs.get(vIter+8)));
			altitude = Double.parseDouble(listOfArgs.get(vIter+9));
			//position of the vehicle at time t in cartesian coords
			Triplet<Double> cartCoords = cartCoordsUsingGeneralTime(latitudeLongitudeToCartesianCoords(latitude, longitude, altitude), vehicleTime);
			
			// what are we doing with this vehicle?
			myVehicle vehicle = new myVehicle(vehicleTime, longitude, latitude, altitude, cartCoords);
			
			for(int j = 0; j < 24; j++)
			{
				mySatellite currSatellite = satellitesClass.getSatellites()[j];
				// check if current satellite above horizon
				if(checkAboveHorizon(currSatellite.sendPos, cartCoords))
				{
					double time = satelliteTimeNewton(vehicleTime, cartCoords, currSatellite);
					currSatellite.sendTime = time;
					currSatellite.sendPos = satellitePositionAtTime(currSatellite, time);
				}
			}
		}
	}
	
	private static boolean checkAboveHorizon(Triplet<Double> xs, Triplet<Double> xv)
	{
		double satNorm = twoNorm(xs);
        double vNorm = twoNorm(xv);
        Triplet<Double> diff = new Triplet<Double>(0.1,0.1,0.1);
        diff.x1 = xs.x1 - xv.x1;
        diff.x2 = xs.x2 - xv.x2;
        diff.x3 = xs.x3 - xv.x3;
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
				writeToLogFile(x);
				listOfArgs.add(x);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listOfArgs;
	}

	
	//FIgure 36: x_s(t) = ...
	// returns the position of satellite sat at time time
	private static Triplet<Double> satellitePositionAtTime(mySatellite sat, double time){
		return
				new Triplet<Double>((satellitesClass.givenRadiusOfPlanet + sat.altitude) * ((sat.uVectorInCartesian.x1 * cos(((TWOPI * time)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x1 * sin(((TWOPI * time)/ sat.period) + sat.phase))),
						(satellitesClass.givenRadiusOfPlanet + sat.altitude) * ((sat.uVectorInCartesian.x2 * cos(((TWOPI * time)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x2 * sin(((TWOPI * time)/ sat.period) + sat.phase))),
						(satellitesClass.givenRadiusOfPlanet + sat.altitude) * ((sat.uVectorInCartesian.x3 * cos(((TWOPI * time)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x3 * sin(((TWOPI * time)/ sat.period) + sat.phase))));

	}
	
	
	// given vehicle time and position in cartesian, returns newton's method for time to send specific satellite info
	private static double satelliteTimeNewton(double vTime, Triplet<Double> vPos, mySatellite sat)
	{
		double[] vector = new double[3];
		Triplet<Double> sPos = satellitePositionAtTime(sat,vTime);
		vector[0] = sPos.x1 - vPos.x1;
		vector[0] = sPos.x2 - vPos.x2;
		vector[0] = sPos.x3 - vPos.x3;
		//start lastTime at t0
		double lastTime = vTime - twoNorm(vector)/satellites.givenSpeedOfLight;
		double nextTime = 0;
		while(Math.abs(nextTime - lastTime) < 0.01/satellites.givenSpeedOfLight) {
			nextTime = lastTime - functionToBeSolvedUsingNewtonsMethod(vTime, vPos, sat, lastTime) / 
					derivativeOfFunctionToBeSolvedUsingNewtonsMethod(vTime, vPos, sat, lastTime);
		}
		return nextTime;
	}
	
	//Figure 37/38:
	private static double functionToBeSolvedUsingNewtonsMethod(double vTime, Triplet<Double> vPos, mySatellite sat, double sTime){
		return Math.pow(satellitePositionAtTime(sat, sTime).x1 - vPos.x1, 2) + 
				Math.pow(satellitePositionAtTime(sat, sTime).x2 - vPos.x2, 2) +
				Math.pow(satellitePositionAtTime(sat, sTime).x3 - vPos.x3, 2) -
				Math.pow(satellites.givenSpeedOfLight, 2) * Math.pow(vTime - sTime, 2);
	}

	
	//Figure 40: Derivative of the function that returns satelliteTime
	private static double derivativeOfFunctionToBeSolvedUsingNewtonsMethod(double vTime, Triplet<Double> vPos, mySatellite sat, double sTime){
		double part1 = ((4 * satellitesClass.givenPi) * (satellitesClass.givenRadiusOfPlanet + sat.altitude)/sat.period);
		Triplet<Double> satPosition = satellitePositionAtTime(sat, sTime);
		Triplet<Double> part2FirstVectorTranspose
				= new Triplet<Double>(satPosition.x1 - vPos.x1,
				satPosition.x2 - vPos.x2,
				satPosition.x3 - vPos.x3
		);

		Triplet<Double> part2SecondVector = new Triplet<Double>(((-sat.uVectorInCartesian.x1 * sin(((TWOPI * sTime)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x1 * cos(((TWOPI * sTime)/ sat.period) + sat.phase))),
				((-sat.uVectorInCartesian.x2 * sin(((TWOPI * sTime)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x2 * cos(((TWOPI * sTime)/ sat.period) + sat.phase))),
				((-sat.uVectorInCartesian.x3 * sin(((TWOPI * sTime)/ sat.period) + sat.phase)) + (sat.vVectorInCartesian.x3 * cos(((TWOPI * sTime)/ sat.period) + sat.phase))));

		double part2Double = (part2FirstVectorTranspose.x1 * part2SecondVector.x1) + (part2FirstVectorTranspose.x2 * part2SecondVector.x2) + (part2FirstVectorTranspose.x3 + part2SecondVector.x3);

		double part3 = 2 * Math.pow(satellitesClass.givenSpeedOfLight, 2) * (vTime - sTime);

		return part1 * part2Double + part3;
		
		
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

	// helper for constructor, uses NS correctly
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
	
	public static double twoNorm(Triplet<Double> vector){
		double sqrAndSum = 0;
		sqrAndSum += vector.x1 * vector.x1;
		sqrAndSum += vector.x2 * vector.x2;
		sqrAndSum += vector.x3 * vector.x3;
		return sqrAndSum;
	}
}

//Class that handles singleton for satellite array and the individual satellite data
//handles initial construction of satellite array.
class satellites {
	public double givenPi;
	public static double givenSpeedOfLight;
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
	// for returning
	public double sendTime;
	public Triplet<Double> sendPos; // in cartesian

	// for reading
	public Triplet<Double> uVectorInCartesian;
	public Triplet<Double> vVectorInCartesian;
	public double period;
	public double phase;
	public double altitude;

	//constructor for latLong
	public mySatellite(int ID, double u1, double u2, double u3, double v1, double v2, double v3, double periodicity, double altitude, double phase){
		this.ID = ID;
		this.uVectorInCartesian = new Triplet<Double>(u1, u2, u3);
		this.vVectorInCartesian = new Triplet<Double>(v1, v2, v3);
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
		this.cartCoords = new Triplet<Double>(cartCoords.x1, cartCoords.x2, cartCoords.x3);
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
