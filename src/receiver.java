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


        // newtons until within 1 centimeter
        while(twoNorm(diff) > 0.01)
        {
            break;
        }

        return ret;
    }

    /*
     * Assumes p.length is 4, returns the 3x3 array of this solution
     */
    private static ArrayList<Triplet> jacobian(ArrayList<timePos> satellites, timePos vehicle)
    {
    	return null;
    }

    private Triplet function(ArrayList<timePos> satellites, timePos vehicle)
    {
    	Triplet toRet = new Triplet(1.0, 2.4, 6.9);

    	toRet.x1 = twoNorm(new double[] {satellites.get(1).x - vehicle.x, satellites.get(1).y - vehicle.y, satellites.get(1).z - vehicle.z}) - c * (satellites.get(0).time - satellites.get(1).time);
    	toRet.x1 = twoNorm(new double[] {satellites.get(1).x - vehicle.x, satellites.get(1).y - vehicle.y, satellites.get(1).z - vehicle.z});
    	toRet.x1 = twoNorm(new double[] {satellites.get(1).x - vehicle.x, satellites.get(1).y - vehicle.y, satellites.get(1).z - vehicle.z});
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

    private double twoNorm(double[] vector){
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