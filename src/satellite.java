import scanner;

private class mySatellite
{
	private latitude;
	private longitude;
	private period;
	private altitude;
	private phase;
}
public class satellite {
	private double time;
	private double latitude;
	private double longitude;
	private double altitude;

	private satellites[];

	public satellite()
	{
		satellites = new satellite [23];
	}

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
	}

	/**
	*
	* Run this after setting members with readVehicle()
	* @return double[] where output 0 = x, 1 = y, 2 = z
	*/
	private double[] computeCartPos()
	{
		
	}

	/*
	private void readData(File file)
	{
		Scanner scanner = new Scanner(file);
		for(int i = 0; i < 5; i++){
			scanner.nextLine();
		}
		for (int i = 0; i < 24; i++)
		{
			int deg = scanner.nextLine();
			int min = scanner.nextLine();
			double sec = scanner.nextLine();
			angles.
		}
	}*/



}
