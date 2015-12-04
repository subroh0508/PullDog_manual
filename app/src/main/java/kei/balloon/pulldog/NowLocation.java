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
    private final static short RFID_SURVEY = 0, GNSS_SURVEY = 1, BOTH_SURVEY = 2;

    private final static double LATITUDE_TO_METER = 111262.393;
    private final static double LONGITUDE_TO_METER = 91158.432; //東京での係数． 任意地点の近似だったら緯度の係数にcos掛ける. 正確には楕円モデルが必要．
    public final static double METER_OF_LATITUDE = 1.0 / LATITUDE_TO_METER;
    public final static double METER_OF_LONGITUDE = 1.0 / LONGITUDE_TO_METER;
    public final static double BLOCK_SIZE = 0.30;

    public boolean SURVEY_EN, CORRECTION_ON;

    private LatLng nowPoint, gnssPoint, pastGnssPoint, rfidPoint;
    private double nowVelocity;

    private int tagId;

    private int visibleSatelite, usefulSatelite;

    private RfidManager RfidLatLngList;
    private Rfid nowRfid = null;

    public NowLocation(RfidManager manager) {
        SURVEY_EN = false;
        CORRECTION_ON = false;

        nowVelocity = 0.0;

        rfidPoint = new LatLng(0.0, 0.0);
        gnssPoint = new LatLng(90.0, 180.0);
        nowPoint = gnssPoint;
        pastGnssPoint = gnssPoint;

        tagId = -1;

        visibleSatelite = 0;
        usefulSatelite = 0;

        RfidLatLngList = manager;
    }

    public void setGnssPoint(String[] rNMEA) {
        pastGnssPoint = gnssPoint;

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
        pastGnssPoint = gnssPoint;

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

    public LatLng getNowPoint() {
        if(tagId != -1) {
            double latDiff = gnssPoint.latitude-pastGnssPoint.latitude;
            double lngDiff = gnssPoint.longitude-pastGnssPoint.longitude;

            nowPoint = new LatLng(rfidPoint.latitude+latDiff, rfidPoint.longitude+lngDiff);
        } else {
            nowPoint = gnssPoint;
        }

        switch(surveyModeSelect()){
            case RFID_SURVEY:
                return rfidPoint;
            case BOTH_SURVEY:
                return nowPoint;
            case GNSS_SURVEY:
                return gnssPoint;
            default:
                return nowPoint;
        }
    }

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

        if(nowRfid != null && !nowRfid.isInDoor())
            rfidPoint = new LatLng(nowRfid.getLat(), nowRfid.getLng());
        
    }

    public boolean isInDoor(){
        if(nowRfid != null && nowRfid.isInDoor())
            return true;
        else
            return false;
    }

    private short surveyModeSelect(){
        double diffRfidAndGnss;

        if(tagId != -1)
            diffRfidAndGnss = getDistance(rfidPoint, nowPoint);
        else
            diffRfidAndGnss = 10000000.0;

        if(diffRfidAndGnss < BLOCK_SIZE*3 || isInDoor())
            return RFID_SURVEY;
        else if(diffRfidAndGnss < BLOCK_SIZE*20)
            return BOTH_SURVEY;
        else
            return GNSS_SURVEY;
    }

    private double getDistance(LatLng p1, LatLng p2){
        double latDiff = (p1.latitude-p2.latitude)*LATITUDE_TO_METER;
        double lngDiff = (p1.longitude-p2.longitude)*LONGITUDE_TO_METER;

        return Math.sqrt(latDiff*latDiff+lngDiff*lngDiff);
    }
}
