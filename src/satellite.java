import scanner;
public class satellite {
	private double time;
	private int latitudeDegrees;
	private int latitudeMinutes;
	private double latitudeSeconds;
	private boolean north;
	private int longitudeDegrees;
	private int longitudeMinutes;
	private double longitudeSeconds;
	private boolean east;
	private double altitude;

	private void read()
	{
		Scanner scanner = new Scanner(inputStream);
		time = Double.parse(scanner.next());
		latitudeDegrees = Integer.parse(scanner.next());
		latitudeMinutes = Integer.parse(scanner.next());
		latitudeSeconds = Double.parse(scanner.next());
		north = Integer.parse(scanner.next()) == 1;
		longitudeDegrees = Integer.parse(scanner.next());
		longitudeMinutes = Integer.parse(scanner.next());
		longitudeSeconds = Double.parse(scanner.next());
		east = Integer.parse(scanner.next()) == 1;
		altitude = Double.parse(scanner.next());
	}
}
