import java.io.*;
import java.util.ArrayList;

import static java.lang.Math.*;
import static java.lang.Math.sin;

//TODO: Solve equations using least squares. At each step, Find out if satellites are above horizon
//TODO: Convert solutions back to latitude and longitude
public class receiver {
    private double[] xv; // change for xv name
    private mySatellite[] satellites;
    private static satellites satelliteClass;

    public static void main(String[] args){
        satelliteClass = new satellites();

        // Satellite args come in via args here.
        ArrayList<String> givenArgs = getArgs();

        // check if satellites are above the horizon

        // divide satellite args into problems
        ArrayList<ArrayList<timePos>> problems =  divideSatellitesIntoGroups(parseGivenSatellitesToArray(givenArgs));

        // solve each problem
        for(ArrayList<timePos> p : problems)
        {
            // solve each problem
            timePos solution = solveProblem(p);
            Triplet cartCoords = cartCoordsUsingGeneralTime(new Triplet(solution.x, solution.y, solution.z), solution.time - p.get(0).time);
            Triplet latLongCoords = cartCoordsToLatLongHeight(cartCoords.x1, cartCoords.x2, cartCoords.x3);
            int latNS = 1;
            int longEW = 1;
            angles latitude = new angles(latLongCoords.x1);
            angles longitude = new angles(latLongCoords.x2);
            if(!latitude.plus){
                latNS = -1;
            }
            if(!longitude.plus){
                longEW = -1;
            }

            writeToLogFile(solution.time + " " + latitude.degrees + " " + latitude.minutes + " " + latitude.seconds + " " + latNS + " " + longitude.degrees + " " + longitude.minutes + " " + longitude.seconds + " " + longEW + " " + latLongCoords.x3, " //receiver output");
        }
    }

    //Excercise 5: convert cartesian coords for t = 0 to latitude, longitude, and height
    //TODO: Guard against division by 0
    //TODO: longitude is between plus and minus pi. atan is between -pi/2 and pi/2
    public static Triplet cartCoordsToLatLongHeight(double x, double y, double z){
        double longitude = 0;
        double latitude = 0;
        double height = 0;
        double[] xyVector = {x, y};
        double[] xyzVector = {x, y, z};

        //Check conditions to find latitude
        if(((x * x) + (y * y)) != 0){
            latitude = atan((double)z/twoNorm(xyVector));
        }

        else if(x == 0 && y == 0 && z > 0){
            latitude = satelliteClass.givenPi/2;
        }

        else if(x == 0 && y == 0 && z < 0){
            latitude = -(satelliteClass.givenPi/2);
        }

        //Check conditions to find longitude
        if(x > 0 && y > 0){
            longitude = atan((double)y/(double)x);
        }
        else if(x < 0){
            longitude = satelliteClass.givenPi + atan((double)y/(double)x);
        }
        else if(x > 0 && y < 0){
            longitude = (2 * satelliteClass.givenPi) + atan((double)y/(double)x);
        }

        //Find height
        height = twoNorm(xyzVector) - satelliteClass.givenRadiusOfPlanet;

        //return new 3-vector with latitude, longitude, and height
        return new Triplet(latitude, longitude, height);
    }

    private static ArrayList<timePos> parseGivenSatellitesToArray(ArrayList<String> args) {
        ArrayList<timePos> inputSats = new ArrayList<>();
        for(String satellite: args){
            String[] satParams = satellite.split(" ");
            mySatellite sat = satelliteClass.getSatellites()[Integer.parseInt(satParams[0])];
            sat.sendTime = Double.parseDouble(satParams[1]);
            sat.sendPos = new Triplet(Double.parseDouble(satParams[2]), Double.parseDouble(satParams[3]), Double.parseDouble(satParams[4]));

            inputSats.add(new timePos(sat.sendTime, sat.sendPos.x1, sat.sendPos.x2, sat.sendPos.x3));
        }
        return inputSats;
    }

    /*
     * returns 4 time positions for each query. Will always sample the first
     * 4 given per query. WILL NOT WORK IF QUERYS ARE GIVEN OUT OF ORDER
     */
    private static ArrayList<ArrayList<timePos>> divideSatellitesIntoGroups(ArrayList<timePos> satellites) {
        ArrayList<ArrayList<timePos>> returnArray = new ArrayList<>();
        double currTime = -1;
        ArrayList<timePos> time = new ArrayList<>();
        // just read the first 4 satellite inputs per query, solve via newton's method
        int satSoFar = 0;
        for(timePos s : satellites)
        {
            if(Math.abs(currTime - s.time) > 1)
            {
                // moving on to next query
                currTime = s.time;
                returnArray.add(time);
                time = new ArrayList<>();
                // reset count for next query
                satSoFar = 0;
            }
            if(++satSoFar > 4)
            // ignore satellites after the first 4
            {continue;}
            else {
                timePos tp = new timePos();
                tp.time = s.time;
                tp.x = s.x;
                tp.y = s.y;
                tp.z = s.z;
                time.add(tp);
            }
        }
        returnArray.remove(0);
        return returnArray;
    }

    /*
     * Solves a problem of 4 timePos variables to return vehicle time and position. Assumes p.length = 4
     */
    private static timePos solveProblem(ArrayList<timePos> p) {
        // start at anything > 0.01
        Triplet diff = new Triplet(1.7,6.9,4.2);

        // start at slc
        double slcLat = angles.rad(40, 45, 55.0, 1);
        double slcLong = angles.rad(111, 50, 58.0, -1);
        double slcAlt = 1372.0;

        timePos v = new timePos();
        Triplet t = latitudeLongitudeToCartesianCoords(slcLat, slcLong, slcAlt);
        v.x = t.x1;
        v.y = t.x2;
        v.z = t.x3;

        // newtons until within 1 centimeter
        while(twoNorm(diff) > 0.01)
        {
            ArrayList<ArrayList<Double>> jacob = jacobian(p, v);
            ArrayList<Double> fun = function(p,v);
        	double[] diffArray =  gaussElimination.solve(new double[][]{{jacob.get(0).get(0),jacob.get(0).get(1), jacob.get(0).get(2)},{jacob.get(1).get(0),jacob.get(1).get(1), jacob.get(1).get(2)},{jacob.get(2).get(0),jacob.get(2).get(1), jacob.get(2).get(2)}}, new double[]{fun.get(0), fun.get(1), fun.get(2)});
            diff = new Triplet(diffArray[0], diffArray[1], diffArray[2]);
            if(!Double.isFinite(diff.x1) || !Double.isFinite(diff.x2) || !Double.isFinite(diff.x3)){
                break;
            }
        	v = v.plus(diff);
        }


        v.time = ((p.get(0).x + p.get(0).y + p.get(0).z) / satelliteClass.givenSpeedOfLight) + p.get(0).time;

        return v;
    }

//    private static Triplet solveByGauss(ArrayList<ArrayList<Double>> jacobian, ArrayList<Double> f) {
//    	f.set(0, -f.get(0));
//    	f.set(1, -f.get(1));
//    	f.set(2, -f.get(2));
//    	Triplet toRet = new Triplet(1.2, 3.4, 5.6);
//    	ArrayList<Double> j0 = jacobian.get(0);
//    	ArrayList<Double> j1 = jacobian.get(1);
//    	ArrayList<Double> j2 = jacobian.get(2);
//    	toRet.x1 = (j0.get(1) * j1.get(2) * f.get(2) - j0.get(1) * j2.get(2) * f.get(1) - j0.get(2) * j1.get(1) * f.get(2) + j0.get(2) * j2.get(1) * f.get(1) + j1.get(1) * j2.get(2) * f.get(0) -
//    			j1.get(2) * j2.get(1) *f.get(0)) / (j0.get(0) * j1.get(1) * j2.get(2) - j0.get(0) * j1.get(2) * j2.get(1) - j0.get(1) * j1.get(0) * j2.get(2) + j0.get(1) * j1.get(2) * j2.get(0)+
//    					j0.get(2) * j1.get(0) * j2.get(1) - j0.get(2) * j1.get(1) * j2.get(0));
//    	toRet.x2 = -1 * (j0.get(0) * j1.get(2) * f.get(2) - j0.get(0) * j2.get(2) * f.get(1) - j0.get(2) * j1.get(0) * f.get(2) + j0.get(2) * j2.get(0) * f.get(1) + j1.get(0) * j2.get(2) * f.get(0) -
//    			j1.get(2) * j2.get(0) *f.get(0)) / (j0.get(0) * j1.get(1) * j2.get(2) - j0.get(0) * j1.get(2) * j2.get(1) - j0.get(1) * j1.get(0) * j2.get(2) + j0.get(1) * j1.get(2) * j2.get(0)+
//    					j0.get(2) * j1.get(0) * j2.get(1) - j0.get(2) * j1.get(1) * j2.get(0));
//    	toRet.x3 = (j0.get(0) * j1.get(1) * f.get(2) - j0.get(0) * j2.get(1) * f.get(1) - j0.get(1) * j1.get(0) * f.get(2) + j0.get(1) * j2.get(0) * f.get(1) + j1.get(0) * j2.get(1) * f.get(0) -
//    			j1.get(1) * j2.get(0) *f.get(0)) / (j0.get(0) * j1.get(1) * j2.get(2) - j0.get(0) * j1.get(2) * j2.get(1) - j0.get(1) * j1.get(0) * j2.get(2) + j0.get(1) * j1.get(2) * j2.get(0)+
//    					j0.get(2) * j1.get(0) * j2.get(1) - j0.get(2) * j1.get(1) * j2.get(0));
//		return toRet;
//	}

    //Excercise 3: converts latitude and longitude position at time t = 0 into cartesian coordinates.
    //CHECKED
    public static Triplet latitudeLongitudeToCartesianCoords(double latitude, double longitude, double altitude){
        Triplet position
                = new Triplet(
                (double) (satelliteClass.givenRadiusOfPlanet + altitude) * cos(latitude) * cos(longitude),
                (double) (satelliteClass.givenRadiusOfPlanet + altitude) * cos(latitude) * sin(longitude),
                (double) (satelliteClass.givenRadiusOfPlanet + altitude) * sin(latitude));
        return position;
    }

	/*
     * Assumes p.length is 4, returns the 3x3 array of this solution
     */
    private static ArrayList<ArrayList<Double>> jacobian(ArrayList<timePos> satellites, timePos vehicle) {
    	ArrayList<ArrayList<Double>> toRet = new ArrayList<>();
        toRet.add(new ArrayList<Double>());
        toRet.add(new ArrayList<Double>());
        toRet.add(new ArrayList<Double>());

    	timePos sat0 = satellites.get(0);
    	timePos sat1 = satellites.get(1);
    	timePos sat2 = satellites.get(2);
    	timePos sat3 = satellites.get(3);

    	double norm1 = twoNorm(sat0.minusPos(vehicle));
    	double norm2 = twoNorm(sat1.minusPos(vehicle));
    	double norm3 = twoNorm(sat2.minusPos(vehicle));
    	double norm4 = twoNorm(sat3.minusPos(vehicle));

    	toRet.get(0).add((sat0.x-vehicle.x)/norm1 - (sat1.x-vehicle.x)/norm2);
    	toRet.get(0).add((sat0.y-vehicle.y)/norm1 - (sat1.y-vehicle.y)/norm2);
    	toRet.get(0).add((sat0.z-vehicle.z)/norm1 - (sat1.z-vehicle.z)/norm2);
    	toRet.get(1).add((sat0.x-vehicle.x)/norm2 - (sat1.x-vehicle.x)/norm3);
    	toRet.get(1).add((sat0.y-vehicle.y)/norm2 - (sat1.y-vehicle.y)/norm3);
    	toRet.get(1).add((sat0.z-vehicle.z)/norm2 - (sat1.z-vehicle.z)/norm3);
    	toRet.get(2).add((sat0.x-vehicle.x)/norm3 - (sat1.x-vehicle.x)/norm4);
    	toRet.get(2).add((sat0.y-vehicle.y)/norm3 - (sat1.y-vehicle.y)/norm4);
    	toRet.get(2).add((sat0.z-vehicle.z)/norm3 - (sat1.z-vehicle.z)/norm4);
    	return toRet;
    }

    private static ArrayList<Double> function(ArrayList<timePos> satellites, timePos vehicle) {
    	ArrayList<Double> toRet = new ArrayList<>();

    	toRet.add( twoNorm(new double[] {satellites.get(1).minusPos(vehicle).x, satellites.get(1).minusPos(vehicle).y, satellites.get(1).minusPos(vehicle).z})
    			- twoNorm(new double[] {satellites.get(0).minusPos(vehicle).x, satellites.get(0).minusPos(vehicle).y, satellites.get(0).minusPos(vehicle).z})
    			- satelliteClass.givenSpeedOfLight * (satellites.get(0).time - satellites.get(1).time));
    	toRet.add( twoNorm(new double[] {satellites.get(2).minusPos(vehicle).x, satellites.get(2).minusPos(vehicle).y, satellites.get(2).minusPos(vehicle).z})
    			- twoNorm(new double[] {satellites.get(1).minusPos(vehicle).x, satellites.get(1).minusPos(vehicle).y, satellites.get(1).minusPos(vehicle).z})
    			- satelliteClass.givenSpeedOfLight * (satellites.get(1).time - satellites.get(1).time));
    	toRet.add( twoNorm(new double[] {satellites.get(3).minusPos(vehicle).x, satellites.get(3).minusPos(vehicle).y, satellites.get(3).minusPos(vehicle).z})
    			- twoNorm(new double[] {satellites.get(2).minusPos(vehicle).x, satellites.get(2).minusPos(vehicle).y, satellites.get(2 ).minusPos(vehicle).z})
    			- satelliteClass.givenSpeedOfLight * (satellites.get(2).time - satellites.get(1).time));
    	return toRet;
    }

    //Excercise 4: converts position in lat and long for general time t into cartesian coordinates.
    //CHECKED
    public static Triplet cartCoordsUsingGeneralTime(Triplet cartCoords, double time){
        double angle = (2 * satelliteClass.givenPi * time)/satelliteClass.givenSiderealDay;

        return new Triplet(
                (cos(angle) * cartCoords.x1) - (sin(angle) * cartCoords.x2),
                (sin(angle) * cartCoords.x1) + (cos(angle) * cartCoords.x2),
                cartCoords.x3);
    }

    public static Triplet rotate(timePos s, int ID, satellites sats) {
        Triplet toRet = new Triplet(0.0, 1.0, 3.9);
        double alpha = -2 * satelliteClass.givenPi * s.time / satelliteClass.givenSiderealDay;
        ArrayList<Triplet> r = new ArrayList<>();
        r.add(new Triplet(Math.cos(alpha), -Math.sin(alpha), 0.0));
        r.add(new Triplet(Math.sin(alpha), Math.cos(alpha), 0.0));
        r.add(new Triplet(0.0, 0.0, 1.0));
        mySatellite curr = sats.getSatellites()[ID];
        Triplet u = curr.uVectorInCartesian;
        Triplet v = curr.vVectorInCartesian;

        return toRet;
    }

    /**
    * returns true if the given position is above the horizon relative to the reciever
    */
	private boolean checkAboveHorizon(double[] xs) {
		double satNorm = twoNorm(xs);
        double myNorm = twoNorm(xv);
        double[] diff = new double[3];
        for(int i = 0; i < 3; i ++)
        {
            diff[i] = xs[i] - xv[i];
        }
        double diffNorm = twoNorm(diff);

        if(diffNorm < twoNorm(new double[]{ satNorm, myNorm }))
        {
            return true;
        }
        return false;
    }

    //Writes the log of standard input and output
    //CHECKED
    private static void writeToLogFile(String arg, String comment){
        File receiverLog = new File("reciever.log");
        //Write stuff here
        if(!(new File("reciever.log").exists())){
            try{
                receiverLog.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("reciever.log", true));
            writer.append(arg + "// " + comment + "\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //CHECKED
    private static ArrayList<String> getArgs(){
        ArrayList<String> listOfArgs = new ArrayList<String>();
        //How we handle piping file contents in as args
        try{
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String x;
            while( (x = input.readLine()) != null ) {
                writeToLogFile(x, "receiver input");
                listOfArgs.add(x);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listOfArgs;
    }

    private static double twoNorm(double[] vector){
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

	public static double twoNorm(timePos vector){
		double sqrAndSum = 0;
		sqrAndSum += vector.x * vector.x;
		sqrAndSum += vector.y * vector.y;
		sqrAndSum += vector.z * vector.z;
		return sqrt(sqrAndSum);
	}

//    public static Triplet test1(){
//        ArrayList<ArrayList<Double>> testarr = new ArrayList<>();
//        ArrayList<Double> testvec = new ArrayList<>();
//
//        testarr.add(new ArrayList<Double>());
//        testarr.add(new ArrayList<Double>());
//        testarr.add(new ArrayList<Double>());
//
//        testarr.get(0).add(2.3);
//        testarr.get(0).add(4.2);
//        testarr.get(0).add(6.9);
//        testarr.get(1).add(2.1);
//        testarr.get(1).add(35.6);
//        testarr.get(1).add(3.5);
//        testarr.get(2).add(5.5);
//        testarr.get(2).add(6.6);
//        testarr.get(2).add(6.9);
//
//        testvec.add(4.7);
//        testvec.add(3.8);
//        testvec.add(6.8);
//
//        return solveByGauss(testarr, testvec);
//    }
}

class timePos {
	public double time;
	public double x;
	public double y;
	public double z;

    public timePos(){}

    public timePos(double time, double x1, double x2, double x3) {
        this.time = time;
        this.x = x1;
        this.y = x2;
        this.z = x3;
    }

    public timePos minusPos(timePos other)
	{
		timePos ret = new timePos();
		ret.time = this.time;
		ret.x = this.x - other.x;
		ret.y = this.y - other.y;
		ret.z = this.z - other.z;
		return ret;
	}

	public timePos plusPos(timePos other)
	{
		timePos ret = new timePos();
		ret.time = this.time;
		ret.x = this.x + other.x;
		ret.y = this.y + other.y;
		ret.z = this.z + other.z;
		return ret;
	}

	public timePos plus(Triplet other)
	{
		timePos ret = new timePos();
		ret.time = this.time;
		ret.x = this.x + other.x1;
		ret.y = this.y + other.x2;
		ret.z = this.z + other.x3;
		return ret;
	}
}

/**
 ** Java Program to Implement Gaussian Elimination Algorithm Found courtesy of
 * https://www.sanfoundry.com/java-program-gaussian-elimination-algorithm/
 **/
/** Class GaussianElimination **/
class gaussElimination
{
    public static double[] solve(double[][] A, double[] B)
    {
        int N = B.length;
        for (int k = 0; k < N; k++)
        {
            /** find pivot row **/
            int max = k;
            for (int i = k + 1; i < N; i++)
                if (Math.abs(A[i][k]) > Math.abs(A[max][k]))
                    max = i;

            /** swap row in A matrix **/
            double[] temp = A[k];
            A[k] = A[max];
            A[max] = temp;

            /** swap corresponding values in constants matrix **/
            double t = B[k];
            B[k] = B[max];
            B[max] = t;

            /** pivot within A and B **/
            for (int i = k + 1; i < N; i++)
            {
                double factor = A[i][k] / A[k][k];
                B[i] -= factor * B[k];
                for (int j = k; j < N; j++)
                    A[i][j] -= factor * A[k][j];
            }
        }

        /** back substitution **/
        double[] solution = new double[N];
        for (int i = N - 1; i >= 0; i--)
        {
            double sum = 0.0;
            for (int j = i + 1; j < N; j++)
                sum += A[i][j] * solution[j];
            solution[i] = (B[i] - sum) / A[i][i];
        }
        return solution;
    }
}

//
////Class that handles singleton for satellite array and the individual satellite data
////handles initial construction of satellite array.
////CHECKED
//class satellites {
//    public double givenPi;
//    public double givenSpeedOfLight;
//    public double givenRadiusOfPlanet;
//    public double givenSiderealDay;
//
//    public final String dataFile = "data.dat";
//    public final int numOfSatellites = 24;
//    public final double degOfOrbitPlane = 55;
//    public final int getNumOfSatellitesOnEachOrbit = 4;
//
//    private mySatellite[] allSatellites;
//
//    public satellites(){
//        if(allSatellites == null || allSatellites.length == 0){
//            this.allSatellites = new mySatellite[24];
//            try{
//                readDataFile();
//            }
//            catch(IOException e){
//                System.out.println("Cannot read: " + dataFile);
//            }
//        }
//        else
//            return;
//    }
//
//    public mySatellite[] getSatellites(){
//        return this.allSatellites;
//    }
//
//    //Reads from data.dat in the same directory as satellites. Writes data into satellite array.
//    //CHECKED
//    private void readDataFile() throws IOException {
//        try(BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
//            int index = 1;
//            String line = br.readLine();
//
//            while (line != null) {
//                String lineWithoutComments = line.split("/=")[0];
//
//                if(index == 1){
//                    givenPi = Double.parseDouble(lineWithoutComments);
//                }
//                else if(index == 2){
//                    givenSpeedOfLight = Double.parseDouble(lineWithoutComments);
//                }
//                else if(index == 3){
//                    givenRadiusOfPlanet = Double.parseDouble(lineWithoutComments);
//                }
//                else if(index == 4){
//                    givenSiderealDay = Double.parseDouble(lineWithoutComments);
//                }
//                else {
//                    int linesOfInfo = 9;
//                    int numOfSatSoFar = 0;
//
//                    while(line != null){
//                        ArrayList<Double> satInfo = new ArrayList<>();
//                        int endIndex = (index - 1) + linesOfInfo;
//
//                        while(index != endIndex + 1){
//                            lineWithoutComments = line.split("/=")[0];
//                            satInfo.add(Double.parseDouble(lineWithoutComments));
//
//                            line = br.readLine();
//                            index++;
//                        }
//
//                        this.allSatellites[numOfSatSoFar] = new mySatellite(numOfSatSoFar,
//                                satInfo.get(0),
//                                satInfo.get(1),
//                                satInfo.get(2),
//                                satInfo.get(3),
//                                satInfo.get(4),
//                                satInfo.get(5),
//                                satInfo.get(6),
//                                satInfo.get(7),
//                                satInfo.get(8));
//
//                        numOfSatSoFar++;
//                        index = endIndex;
//                    }
//
//                    return;
//                }
//
//                line = br.readLine();
//                index++;
//            }
//        }
//    }
//}
//
//
//class mySatellite {
//    public final int ID;
//    // for returning
//    public double sendTime;
//    public Triplet sendPos; // in cartesian
//
//    // for reading
//    public Triplet uVectorInCartesian;
//    public Triplet vVectorInCartesian;
//    public double period;
//    public double phase;
//    public double altitude;
//
//    //constructor for latLong
//    public mySatellite(int ID, double u1, double u2, double u3, double v1, double v2, double v3, double periodicity, double altitude, double phase){
//        this.ID = ID;
//        this.uVectorInCartesian = new Triplet(u1, u2, u3);
//        this.vVectorInCartesian = new Triplet(v1, v2, v3);
//        this.period = periodicity;
//        this.altitude = altitude;
//        this.phase = phase;
//    }
//}
//
//
//// for testing?
//class myVehicle{
//    public double lat;
//    public double longitude;
//    public double alt;
//    public double time;
//    public Triplet cartCoords;
//
//    public myVehicle(double time, double longitude, double lat, double alt, Triplet cartCoords){
//        this.time = time;
//        this.longitude = longitude;
//        this.lat = lat;
//        this.alt = alt;
//        this.cartCoords = new Triplet(cartCoords.x1, cartCoords.x2, cartCoords.x3);
//    }
//}
//
//class Four_Tuple<T> {
//    public T x1;
//    public T x2;
//    public T x3;
//    public T x4;
//
//    public Four_Tuple(T x1, T x2, T x3, T x4){
//        this.x1 = x1;
//        this.x2 = x2;
//        this.x3 = x3;
//        this.x4 = x4;
//    }
//}
//
//class Triplet {
//    public double x1;
//    public double x2;
//    public double x3;
//
//    public Triplet(double x1, double x2, double x3){
//        this.x1 = x1;
//        this.x2 = x2;
//        this.x3 = x3;
//    }
//
//    public Triplet minus(Triplet other)
//    {
//        return new Triplet( (this.x1) - (other.x1),
//                (this.x2) - (other.x2),
//                (this.x3) - (other.x3));
//    }
//}