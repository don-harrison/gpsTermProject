
import model.Triplet;
import model.Tuple;

import java.lang.reflect.Array;

import static java.lang.Math.*;
import java.lang.Scanner;

public class mySatellite
{
	private int ID;
	private double latitude; // in radians
	private double longitude;
	private double period;
	private double altitude;
	private double phase;
}

public class satellite {
	private double time;
	private double latitude;
	private double longitude;
	private double altitude;

	private double pi;
	private double c;
	private double r;
	private double s;

	private mySatellite[] satellites;

	public satellite()
	{
		satellites = new mySatellite [23];
	}


	public static main(String args[])
	{
		time = Double.parseDouble(args[0]);
		latitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(args[1]),  Double.parseDouble(args[2]),  Double.parseDouble(args[3]),  Double.parseDouble(args[4]));
		longitude = degMinSecToLatitudeOrLongitude( Double.parseDouble(args[5]),  Double.parseDouble(args[6]),  Double.parseDouble(args[7]),  Double.parseDouble(args[8]));
		altitude = Double.parseDouble(args[9]);
	}
	
	// helper for constructor 
	public static Double degMinSecToLatitudeOrLongitude(double deg, double min, double sec, int NS){
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
	*
	* Run this after setting members with readVehicle()
	* @return double[] where output 0 = x, 1 = y, 2 = z corresponding to 
	* position of vehicle at given time 
	*/
	private double[] computeCartPos()
	{
		double angle = 2 * pi * time / s;
		double[][] rotationMatrix = [[Math.cos(angle), -Math.sin(angle), 0],[Math.sin(angle), Math.cos(angle), 0],[0, 0, 1]];
		double[] xTime0 = [(r+altitude)*Math.cos(latitude)*Math.cos(longitude), (r+altitude)*Math.cos(latitude)*Math.sin(longitude), (r+altitude) * Math.sin(latitude)];
		return rotationMatrix * xTime0;
	}

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
