import java.io.*;
import java.util.ArrayList;
//TODO: Solve equations using least squares. At each step, Find out if satellites are above horizon
//TODO: Convert solutions back to latitude and longitude
public class receiver {
    private double[] xv; // change for xv name
    private mySatellite[] satellites;
    private static satellites satelliteClass;

    // TODO: read in values
    private static double pi = 3.141592653589793116;
    private static double r  = 6.367444500000000000E+06;
    private static double siderealDay = 8.616408999999999651E+04;
    private static double c = 2.997924580000000000E+08;

    public static void main(String[] args){
    	/*
    	System.out.println("asdf");
    	
    	ArrayList<ArrayList<Double>> testarr = new ArrayList<>();
    	ArrayList<Double> testvec = new ArrayList<>();

    	testarr.add(new ArrayList<Double>());
    	testarr.add(new ArrayList<Double>());
    	testarr.add(new ArrayList<Double>());
    	
    	testarr.get(0).add(2.3);
    	testarr.get(0).add(4.2);
    	testarr.get(0).add(6.9);
    	testarr.get(1).add(2.1);
    	testarr.get(1).add(35.6);
    	testarr.get(1).add(3.5);
    	testarr.get(2).add(5.5);
    	testarr.get(2).add(6.6);
    	testarr.get(2).add(6.9);

    	testvec.add(4.7);
    	testvec.add(3.8);
    	testvec.add(6.8);
    	
    	System.out.println(solveByGauss(testarr, testvec).x3);
    	*/
    	
        satelliteClass = new satellites();
        // Satellite args come in via args here.
        ArrayList<mySatellite> satellites = new ArrayList<>();
        // check if satellites are above the horizon

        // divide satellite args into problems
        ArrayList<ArrayList<timePos>> problems =  divideSatellitesIntoGroups(satellites);
        // solve each problem
        for(ArrayList<timePos> p : problems)
        {
            // solve each problem
        }
    }

    private static ArrayList<mySatellite> parseGivenSatellites(){
        ArrayList<mySatellite> givenSatellites = new ArrayList<>();

        return givenSatellites;
    }

    /*
     * returns 4 time positions for each query. Will always sample the first
     * 4 given per query. WILL NOT WORK IF QUERYS ARE GIVEN OUT OF ORDER
     */
    private static ArrayList<ArrayList<timePos>> divideSatellitesIntoGroups(ArrayList<mySatellite> satellites)
    {
        ArrayList<ArrayList<timePos>> returnArray = new ArrayList<>();
        double currTime = -1;
        ArrayList<timePos> time = new ArrayList<>();
        // just read the first 4 satellite inputs per query, solve via newton's method
        int satSoFar = 0;
        for(mySatellite s : satellites)
        {
            if(Math.abs(currTime - s.sendTime) > 1)
            {
                // moving on to next query
                currTime = s.sendTime;
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
                tp.time = s.sendTime;
                tp.x = s.sendPos.x1;
                tp.y = s.sendPos.x2;
                tp.z = s.sendPos.x3;
                time.add(tp);
            }
        }

        return returnArray;
    }

    /*
     * Solves a problem of 4 timePos variables to return vehicle time and position. Assumes p.length = 4
     */
    private static timePos solveProblem(ArrayList<timePos> p)
    {
        timePos ret = new timePos();
        Triplet diff = new Triplet(1.7,6.9,4.2);

        timePos v = p.get(0);

        // newtons until within 1 centimeter
        while(twoNorm(diff) > 0.01)
        {
        	diff = solveByGauss(jacobian(p, v), function(p,v));
        }

        return ret;
    }

    private static Triplet solveByGauss(ArrayList<ArrayList<Double>> jacobian, ArrayList<Double> f) {
    	Triplet toRet = new Triplet(1.2, 3.4, 5.6);
    	ArrayList<Double> j0 = jacobian.get(0);
    	ArrayList<Double> j1 = jacobian.get(1);
    	ArrayList<Double> j2 = jacobian.get(2);
    	toRet.x1 = (j0.get(1) * j1.get(2) * f.get(2) - j0.get(1) * j2.get(2) * f.get(1) - j0.get(2) * j1.get(1) * f.get(2) + j0.get(2) * j2.get(1) * f.get(1) + j1.get(1) * j2.get(2) * f.get(0) -
    			j1.get(2) * j2.get(1) *f.get(0)) / (j0.get(0) * j1.get(1) * j2.get(2) - j0.get(0) * j1.get(2) * j2.get(1) - j0.get(1) * j1.get(0) * j2.get(2) + j0.get(1) * j1.get(2) * j2.get(0)+
    					j0.get(2) * j1.get(0) * j2.get(1) - j0.get(2) * j1.get(1) * j2.get(0));
    	toRet.x2 = -1 * (j0.get(0) * j1.get(2) * f.get(2) - j0.get(0) * j2.get(2) * f.get(1) - j0.get(2) * j1.get(0) * f.get(2) + j0.get(2) * j2.get(0) * f.get(1) + j1.get(0) * j2.get(2) * f.get(0) -
    			j1.get(2) * j2.get(0) *f.get(0)) / (j0.get(0) * j1.get(1) * j2.get(2) - j0.get(0) * j1.get(2) * j2.get(1) - j0.get(1) * j1.get(0) * j2.get(2) + j0.get(1) * j1.get(2) * j2.get(0)+
    					j0.get(2) * j1.get(0) * j2.get(1) - j0.get(2) * j1.get(1) * j2.get(0));
    	toRet.x3 = (j0.get(0) * j1.get(1) * f.get(2) - j0.get(0) * j2.get(1) * f.get(1) - j0.get(1) * j1.get(0) * f.get(2) + j0.get(1) * j2.get(0) * f.get(1) + j1.get(0) * j2.get(1) * f.get(0) -
    			j1.get(1) * j2.get(0) *f.get(0)) / (j0.get(0) * j1.get(1) * j2.get(2) - j0.get(0) * j1.get(2) * j2.get(1) - j0.get(1) * j1.get(0) * j2.get(2) + j0.get(1) * j1.get(2) * j2.get(0)+
    					j0.get(2) * j1.get(0) * j2.get(1) - j0.get(2) * j1.get(1) * j2.get(0));
		return toRet;
	}

	/*
     * Assumes p.length is 4, returns the 3x3 array of this solution
     */
    private static ArrayList<ArrayList<Double>> jacobian(ArrayList<timePos> satellites, timePos vehicle)
    {
    	ArrayList<ArrayList<Double>> toRet = new ArrayList<>();
    	
    	timePos sat0 = satellites.get(0);
    	timePos sat1 = satellites.get(1);
    	timePos sat2 = satellites.get(2);
    	timePos sat3 = satellites.get(3);
    	
    	double norm1 = twoNorm(sat0.minusPos(vehicle));
    	double norm2 = twoNorm(sat1.minusPos(vehicle));
    	double norm3 = twoNorm(sat2.minusPos(vehicle));
    	double norm4 = twoNorm(sat3.minusPos(vehicle));
    	
    	toRet.get(0).set(0, sat0.x-vehicle.x/norm1 - sat1.x-vehicle.x/norm2);
    	toRet.get(0).set(1, sat0.y-vehicle.y/norm1 - sat1.y-vehicle.y/norm2);
    	toRet.get(0).set(2, sat0.z-vehicle.z/norm1 - sat1.z-vehicle.z/norm2);
    	toRet.get(1).set(0, sat0.x-vehicle.x/norm2 - sat1.x-vehicle.x/norm3);
    	toRet.get(1).set(1, sat0.y-vehicle.y/norm2 - sat1.y-vehicle.y/norm3);
    	toRet.get(1).set(2, sat0.z-vehicle.z/norm2 - sat1.z-vehicle.z/norm3);
    	toRet.get(2).set(0, sat0.x-vehicle.x/norm3 - sat1.x-vehicle.x/norm4);
    	toRet.get(2).set(1, sat0.y-vehicle.y/norm3 - sat1.y-vehicle.y/norm4);
    	toRet.get(2).set(2, sat0.z-vehicle.z/norm3 - sat1.z-vehicle.z/norm4);
    	return toRet;
    }

    private static ArrayList<Double> function(ArrayList<timePos> satellites, timePos vehicle)
    {
    	ArrayList<Double> toRet = new ArrayList<>();

    	toRet.add( twoNorm(new double[] {satellites.get(1).minusPos(vehicle).x, satellites.get(1).minusPos(vehicle).y, satellites.get(1).minusPos(vehicle).z})
    			- twoNorm(new double[] {satellites.get(0).minusPos(vehicle).x, satellites.get(0).minusPos(vehicle).y, satellites.get(0).minusPos(vehicle).z})
    			- c * (satellites.get(0).time - satellites.get(1).time));
    	toRet.add( twoNorm(new double[] {satellites.get(2).minusPos(vehicle).x, satellites.get(2).minusPos(vehicle).y, satellites.get(2).minusPos(vehicle).z})
    			- twoNorm(new double[] {satellites.get(1).minusPos(vehicle).x, satellites.get(1).minusPos(vehicle).y, satellites.get(1).minusPos(vehicle).z})
    			- c * (satellites.get(1).time - satellites.get(1).time));
    	toRet.add( twoNorm(new double[] {satellites.get(3).minusPos(vehicle).x, satellites.get(3).minusPos(vehicle).y, satellites.get(3).minusPos(vehicle).z})
    			- twoNorm(new double[] {satellites.get(2).minusPos(vehicle).x, satellites.get(2).minusPos(vehicle).y, satellites.get(2 ).minusPos(vehicle).z})
    			- c * (satellites.get(2).time - satellites.get(1).time));
    	return toRet;
    }

    /**
    * returns true if the given position is above the horizon relative to the reciever
    */
	private boolean checkAboveHorizon(double[] xs)
	{
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

    /**
     * returns a list of ID's of all satellites above the horizon
     */
    private int[] checkSatellites()
    {
        for(int i = 0; i < satellites.length / 3; i++)
        {
            if(true)
            {
                return null;
            }
            //TODO finish
        }
        return null;
    }

    /**
     * reads from input stream
     */
    private void readInput()
    {
        //TODO: this
    }

    //Writes the log of standard input and output
    //CHECKED
    private static void writeToLogFile(String arg, String comment){
        File satelliteLog = new File("reciever.log");
        //Write stuff here
        if(!(new File("reciever.log").exists())){
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
        return sqrAndSum;
    }    
    
	public static double twoNorm(Triplet vector){
		double sqrAndSum = 0;
		sqrAndSum += vector.x1 * vector.x1;
		sqrAndSum += vector.x2 * vector.x2;
		sqrAndSum += vector.x3 * vector.x3;
		return sqrAndSum;
	}
	
	public static double twoNorm(timePos vector){
		double sqrAndSum = 0;
		sqrAndSum += vector.x * vector.x;
		sqrAndSum += vector.y * vector.y;
		sqrAndSum += vector.z * vector.z;
		return sqrAndSum;
	}
}

class timePos{
	public double time;
	public double x;
	public double y;
	public double z;

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