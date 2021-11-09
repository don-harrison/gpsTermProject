//import Scanner;
//import Math;
//
//import java.io.File;
//import java.io.InputStream;
//import java.util.Scanner;
//
////private class mySatellite
////{
////	private latitude;
////	private longitude;
////	private period;
////	private altitude;
////	private phase;
////}
//public class satellite {
//	private double time;
//	private double latitude;
//	private double longitude;
//	private double altitude;
//
//	private double pi;
//	private double c;
//	private double r;
//	private double s;
//
//	private satellite[] satellites;
//
//	public satellite()
//	{
//		satellites = new satellite [23];
//	}
//
//	private void readVehicle(InputStream inputStream)
//	{
//		Scanner scanner = new Scanner(inputStream);
//		time = Double.parseDouble(scanner.next());
//		int deg = Integer.parseInt(scanner.next());
//		int min = Integer.parseInt(scanner.next());
//		double sec = Double.parseDouble(scanner.next());
//		north = Integer.parseInt(scanner.next()) == 1;
//		latitude = angles.rad(deg, min, sec, north);
//		deg = Integer.parseInt(scanner.next());
//		min = Integer.parseInt(scanner.next());
//		east = Integer.parseInt(scanner.next()) == 1;
//		longitude = angles.rad(deg, min, sec, east);
//		altitude = Double.parseDouble(scanner.next());
//	}
//
//	private void readData(File file)
//	{
//		try{
//			Scanner scanner = new Scanner(file);
//			pi = scanner.nextLine();
//			c = scanner.nextLine();
//			r = scanner.nextLine();
//			s = scanner.nextLine();
//			// TODO: satellites??
//			for (int i = 0; i < 24; i++)
//			{
//				int deg = scanner.nextLine();
//				int min = scanner.nextLine();
//				double sec = scanner.nextLine();
//				angles.
//			}
//		}
//		catch(Exception e){
//			System.out.println(e.getMessage());
//		}
//
//	}
//
//
//
//}
