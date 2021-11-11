package src;

public class angles {

    double radians, seconds;
    int degrees, minutes;
    boolean plus;

    final static double pi = 2 * java.lang.Math.acos(0.0);
    final static double twopi = 2.0 * pi;

    //Excercise 2: converts angles from radians to degrees, minutes, and seconds
    public angles(double rad) {
        radians = rad;
        if (radians < -pi || radians > pi) {
            int n = (int) ((radians + pi) / twopi);
            if (radians < 0.0) {
                n--;
            }
            radians = radians - n * twopi;
        }
        double theta = radians;
        if (theta >= 0.0) {
            plus = true;
        } else {
            plus = false;
            theta = -theta;
        }
        double degs = theta * 360 / twopi;
        degrees = (int) degs;
        degs = degs - degrees;
        degs = 60 * degs;
        minutes = (int) degs;
        degs = degs - minutes;
        seconds = 60 * degs;
        if (java.lang.Math.abs(60 - seconds) < 1.0E-3) {
            seconds = 0;
            minutes++;
        }
        if (minutes == 60) {
            minutes = 0;
            degrees++;
        }
    }

    //Excercise 2: converts angles from degrees, minutes, and seconds to radians
    public angles(int deg, int min, int sec, boolean pm) {
        degrees = deg;
        minutes = min;
        seconds = sec;
        plus = pm;
        radians = rad(deg, min, sec, pm);
    }

    static double rad(int deg, int min, double sec, int NS) {
        if (NS == 1) {
            return rad(deg, min, sec, true);
        } else if (NS == -1) {
            return rad(deg, min, sec, false);
        } else {
            return 0;
        }
    }

    static double rad(int deg, int min, double sec, boolean pm) {
        double Deg = (double) deg;
        double Min = (double) min;
        double result = twopi / 360 * (Deg + Min / 60 + sec / 60 / 60);
        if (!pm) {
            result = -result;
        }
        return result;
    }
}
