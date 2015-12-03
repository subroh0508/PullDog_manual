package kei.balloon.pulldog;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by root on 15/09/04.
 */
public class NowLocation implements Serializable{
    public boolean SURVEY_EN, CORRECTION_ON;

    private LatLngHeight mPoint;
    private double mVelocity;

    private int mVisible, mUseful;

    public NowLocation() {
        SURVEY_EN = false;
        CORRECTION_ON = false;

        mPoint = new LatLngHeight();
        mVelocity = 0.0;

        mVisible = 0;
        mUseful = 0;
    }

    public void setPoint(String[] rNMEA) {
        if(rNMEA[6].charAt(0) == 'N' && rNMEA[6].charAt(1) == 'N')
            SURVEY_EN = false;
        else
            SURVEY_EN = true;

        if(rNMEA[6].charAt(0) == 'D' || rNMEA[6].charAt(1) == 'D')
            CORRECTION_ON = true;
        else
            CORRECTION_ON = false;


        double valuelat = Double.valueOf(rNMEA[2]), valuelng = Double.valueOf(rNMEA[4]);;
        double deglat = Math.floor(valuelat / 100.0), deglng = Math.floor(valuelng / 100.0);

        mPoint.setParameter(calculateLatLng(deglat, valuelat - deglat * 100),
                calculateLatLng(deglng, valuelng - deglng * 100),
                Double.valueOf(rNMEA[9]), rNMEA[3].charAt(0), rNMEA[5].charAt(0));
    }

    public void setPoint(String indicator, double lat, double lng, double height, char NS, char EW) {
        if(indicator.charAt(0) == 'N' && indicator.charAt(1) == 'N')
            SURVEY_EN = false;
        else
            SURVEY_EN = true;

        if(indicator.charAt(0) == 'D' || indicator.charAt(1) == 'D')
            CORRECTION_ON = true;
        else
            CORRECTION_ON = false;

        double deglat = Math.floor(lat / 100.0), deglng = Math.floor(lng / 100.0);

        mPoint.setParameter(calculateLatLng(deglat, lat - deglat * 100),
                calculateLatLng(deglng, lng - deglng * 100), height, NS ,EW);
    }

    public void setVelocity(String[] rNMEA){
        if(!rNMEA[7].isEmpty())
            mVelocity = Double.valueOf(rNMEA[7]);
        else
            mVelocity = 0.0;
    }

    public void setVelocity(double v) { mVelocity = v; }

    public void setVisibleSatelite(int n) { mVisible = n; }

    public void setUsefulSatelite(int n) {
        mUseful = n;
    }

    public double getPointLat() { return mPoint.getLat(); }

    public double getPointLng() { return mPoint.getLng(); }

    public double getPointHeight() {
        return mPoint.getHeight();
    }

    public LatLngHeight getPoint() { return mPoint; }

    public char getDirectionNS() { return mPoint.getNS(); }

    public char getDirectionEW() {
        return mPoint.getEW();
    }

    public char[] getDirection() {
        return mPoint.getDirection();
    }

    public int getVisibleSatelite() {
        return mVisible;
    }

    public int getUsefulSatelite() { return mUseful; }

    public double getVelocity() { return mVelocity; }

    private double calculateLatLng(double d, double m) {
        int digit = 8;
        BigDecimal deg = new BigDecimal(d);
        BigDecimal min = new BigDecimal(m);

        BigDecimal result = deg.add(min.divide(new BigDecimal(60.0), digit, BigDecimal.ROUND_HALF_UP));

        return result.doubleValue();
    }

}
