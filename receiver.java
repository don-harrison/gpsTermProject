//
////TODO: Find out if satellites are above horizon
////TODO: Solve equations using least squares.
////TODO: Convert solutions back to latitude and longitude
//public class receiver {
//	private double[] posv; // change for xv name
//    private mySatellite[] satellites;
//
//    public static void main(String[] args){
//        //Satellite args come in via args here.
//
//
//    }
//    /**
//    * returns true if the given position is above the horizon relative to the reciever
//    */
//	private boolean checkAboveHorizon(double[] xs)
//	{
//		double satNorm = twoNorm(xs);
//        double myNorm = twoNorm(posv);
//        double[] diff = new double[3];
//        for(int i = 0; i < 3; i ++)
//        {
//            diff[i] = xs[i] - posv[i];
//        }
//        double diffNorm = twoNorm(diff);
//
//        if(diffNorm < twoNorm(new double[]{ satNorm, myNorm }))
//        {
//            return true;
//        }
//	}
//
//    /**
//    * returns a list of ID's of all satellites above the horizon
//    */
//    private int[] checkSatellites()
//    {
//        for(int i = 0; i < satellites.length / 3; i++)
//        {
//            if(checkAboveHorizon())
//            //TODO finish
//        }
//    }
//
//    /**
//    * reads from input stream
//    */
//    private void read()
//    {
//        //TODO: this
//    }
//
//
//    private double twoNorm(double[] vector){
//        double sqrAndSum = 0;
//        for(double element: vector){
//            sqrAndSum += (element * element);
//        }
//        return sqrAndSum;
//    }
//}
