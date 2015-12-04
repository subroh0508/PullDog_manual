package kei.balloon.pulldog;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 15/09/04.
 */
public class NowLocation implements Serializable{
    private final static boolean RFID_SURVEY = true, GNSS_SURVEY = false;

    public boolean SURVEY_EN, CORRECTION_ON;

    private LatLng nowPoint, gnssPoint, rfidPoint;
    private double nowVelocity;

    private int tagId;

    private int visibleSatelite, usefulSatelite;

    private RfidManager RfidLatLngList;
    private Rfid nowRfid;

    public NowLocation(RfidManager manager) {
        SURVEY_EN = false;
        CORRECTION_ON = false;

        nowVelocity = 0.0;

        nowPoint = new LatLng(0.0, 0.0);

        tagId = -1;

        visibleSatelite = 0;
        usefulSatelite = 0;

        RfidLatLngList = manager;
    }

    public void setGnssPoint(String[] rNMEA) {
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

        if(rNMEA[3].charAt(0) == 'S') {
            gnssPoint = new LatLng(-1 * calculateLatLng(deglat, valuelat - deglat * 100),
                    calculateLatLng(deglng, valuelng - deglng * 100));
        } else if(rNMEA[5].charAt(0) == 'W') {
            gnssPoint = new LatLng(calculateLatLng(deglat, valuelat - deglat * 100),
                    -1*calculateLatLng(deglng, valuelng - deglng * 100));
        } else {
            gnssPoint = new LatLng(calculateLatLng(deglat, valuelat - deglat * 100),
                    calculateLatLng(deglng, valuelng - deglng * 100));
        }

    }

    public void setGnssPoint(String indicator, double lat, double lng, double height, char NS, char EW) {
        if(indicator.charAt(0) == 'N' && indicator.charAt(1) == 'N')
            SURVEY_EN = false;
        else
            SURVEY_EN = true;

        if(indicator.charAt(0) == 'D' || indicator.charAt(1) == 'D')
            CORRECTION_ON = true;
        else
            CORRECTION_ON = false;

        double deglat = Math.floor(lat / 100.0), deglng = Math.floor(lng / 100.0);

        if(NS == 'S') {
            gnssPoint = new LatLng(-1*calculateLatLng(deglat, lat - deglat * 100),
                    calculateLatLng(deglng, lng - deglng * 100));
        } else if(EW == 'W') {
            gnssPoint = new LatLng(calculateLatLng(deglat, lat - deglat * 100),
                    -1*calculateLatLng(deglng, lng - deglng * 100));
        } else {
            gnssPoint = new LatLng(calculateLatLng(deglat, lat - deglat * 100),
                    calculateLatLng(deglng, lng - deglng * 100));
        }
    }

    public void setVelocity(String[] rNMEA){
        if(!rNMEA[7].isEmpty())
            nowVelocity = Double.valueOf(rNMEA[7]);
        else
            nowVelocity = 0.0;
    }

    public void setVelocity(double v) { nowVelocity = v; }

    public void setVisibleSatelite(int n) { visibleSatelite = n; }

    public void setUsefulSatelite(int n) { usefulSatelite = n; }

    public double getNowPointLat() { return nowPoint.latitude; }

    public double getNowPointLng() { return nowPoint.longitude; }

    public LatLng getNowPoint() { return nowPoint; }

    public char getDirectionNS() {
        if(Math.signum(nowPoint.latitude) == -1.0) {
            return 'S';
        } else {
            return 'N';
        }
    }

    public char getDirectionEW() {
        if(Math.signum(nowPoint.latitude) == -1.0) {
            return 'W';
        } else {
            return 'E';
        }
    }

    public char[] getDirection() {
        char[] direction = new char[2];

        if(Math.signum(nowPoint.latitude) == -1.0) {
            direction[0] = 'S';
        } else {
            direction[0] = 'N';
        }

        if(Math.signum(nowPoint.latitude) == -1.0) {
            direction[1] = 'W';
        } else {
            direction[1] = 'E';
        }

        return direction;
    }

    public int getVisibleSatelite() {
        return visibleSatelite;
    }

    public int getUsefulSatelite() { return usefulSatelite; }

    public double getVelocity() { return nowVelocity; }

    private double calculateLatLng(double d, double m) {
        int digit = 8;
        BigDecimal deg = new BigDecimal(d);
        BigDecimal min = new BigDecimal(m);

        BigDecimal result = deg.add(min.divide(new BigDecimal(60.0), digit, BigDecimal.ROUND_HALF_UP));

        return result.doubleValue();
    }

    public void setTagId(int tag){ 
        tagId = tag;
        
        nowRfid = RfidLatLngList.getRfidById(tag);

        if(!nowRfid.isInDoor())
            rfidPoint = new LatLng(nowRfid.getLat(), nowRfid.getLng());
        
    }

    public boolean isInDoor(){
        if(nowRfid.isInDoor())
            return true;
        else
            return false;
    }

    private boolean surveyModeSelect(){

    }

    private double getDirection(LatLng p1, LatLng p2){
        BigDecimal latDiff = new BigDecimal(p1.latitude).subtract(new BigDecimal(p2.latitude));
        BigDecimal lngDiff = new BigDecimal(p1.longitude).subtract(new BigDecimal(p2.longitude));



    }
}
