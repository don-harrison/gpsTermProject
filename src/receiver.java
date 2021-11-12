package src;

import java.util.ArrayList;
//TODO: Solve equations using least squares. At each step, Find out if satellites are above horizon
//TODO: Convert solutions back to latitude and longitude
public class receiver {
	private double[] xv; // change for xv name
    private mySatellite[] satellites;

    // TODO: read in values
    private static double pi = 3.141592653589793116;
    private static double r  = 6.367444500000000000E+06;
    private static double siderealDay = 8.616408999999999651E+04;
    private static double c = 2.997924580000000000E+08;

    public static void main(String[] args){
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
    	Triplet<Double> diff = new Triplet<Double>(1.7,6.9,4.2);
    	
    	
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
    
    private Triplet<Double> function(ArrayList<timePos> satellites, timePos vehicle)
    {
    	Triplet<Double> toRet = new Triplet<>(1.0, 2.4, 6.9);

    	toRet.x1 = twoNorm(new double[] {satellites.get(1).minusPos(vehicle).x, satellites.get(1).minusPos(vehicle).y, satellites.get(1).minusPos(vehicle).z})
    			- twoNorm(new double[] {satellites.get(0).minusPos(vehicle).x, satellites.get(0).minusPos(vehicle).y, satellites.get(0).minusPos(vehicle).z})
    			- c * (satellites.get(0).time - satellites.get(1).time);
    	toRet.x2 = twoNorm(new double[] {satellites.get(2).minusPos(vehicle).x, satellites.get(2).minusPos(vehicle).y, satellites.get(2).minusPos(vehicle).z})
    			- twoNorm(new double[] {satellites.get(1).minusPos(vehicle).x, satellites.get(1).minusPos(vehicle).y, satellites.get(1).minusPos(vehicle).z})
    			- c * (satellites.get(1).time - satellites.get(1).time);
    	toRet.x3 = twoNorm(new double[] {satellites.get(3).minusPos(vehicle).x, satellites.get(3).minusPos(vehicle).y, satellites.get(3).minusPos(vehicle).z})
    			- twoNorm(new double[] {satellites.get(2).minusPos(vehicle).x, satellites.get(2).minusPos(vehicle).y, satellites.get(2 ).minusPos(vehicle).z})
    			- c * (satellites.get(2).time - satellites.get(1).time);
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
    
    private void readData()
    {
    	//TODO: read in 1st 4 lines of data.dat
    }


    private double twoNorm(double[] vector){
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