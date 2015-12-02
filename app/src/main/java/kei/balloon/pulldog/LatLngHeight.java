package kei.balloon.pulldog;

import java.util.Arrays;

/**
 * Created by root on 15/09/09.
 */
public class LatLngHeight {
    private double[] latlng = new double[2];
    private double height;
    private char[] direction = new char[2];

    public LatLngHeight() {
        latlng[0] = 0.0;
        latlng[1] = 0.0;
        height = 0.0;

        direction[0] = 'N';
        direction[1] = 'E';
    }

    public LatLngHeight(double lat, double lng, double h, char NS, char EW) {
        latlng[0] = lat;
        latlng[1] = lng;
        height = h;

        direction[0] = NS;
        direction[1] = EW;
    }

    public LatLngHeight(double[] p, char[] d) {
        latlng[0] = p[0];
        latlng[1] = p[1];
        height = p[2];

        direction = Arrays.copyOf(d, 2);
    }

    public LatLngHeight(double[] p, double h, char[] d) {
        latlng = Arrays.copyOf(p, 2);
        height = h;

        direction = Arrays.copyOf(d, 2);
    }

    public void setParameter(double lat, double lng, double h, char NS, char EW) {
        latlng[0] = lat;
        latlng[1] = lng;
        height = h;

        direction[0] = NS;
        direction[1] = EW;
    }

    public void setParameter(double[] p, char[] d) {
        latlng[0] = p[0];
        latlng[1] = p[1];
        height = p[2];

        direction = Arrays.copyOf(d, 2);
    }

    public void setParameter(double[] p, double h, char[] d) {
        latlng = Arrays.copyOf(p, 2);
        height = h;

        direction = Arrays.copyOf(d, 2);
    }

    public String toString() {
        return direction[0]+":"+latlng[0]+","
                +direction[1]+":"+latlng[1]+","
                +"H:"+height;
    }

    public double[] getLatLng() { return latlng; }

    public char[] getDirection() { return direction; }

    public double getLat() { return latlng[0]; }

    public double getLng() { return latlng[1]; }

    public double getHeight() { return height; }

    public double[] getPoint() {
        double[] ret = new double[3];

        ret[0] = latlng[0];
        ret[1] = latlng[1];
        ret[2] = height;

        return ret;
    }

    public char getNS() { return direction[0]; }

    public char getEW() { return direction[1]; }
}
