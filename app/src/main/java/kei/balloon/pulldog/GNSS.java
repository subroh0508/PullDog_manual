package kei.balloon.pulldog;

import java.util.ArrayList;

/**
 * Created by root on 15/09/04.
 */
public class GNSS {
    public boolean GPS_ON, GLONASS_ON;

    private ArrayList<Integer> mVisibleGNSS;
    private ArrayList<Integer> mUsefulGNSS;
    private ArrayList<SatelitePotision> mGNSSPotision;
    private ArrayList<Integer> mGNSSSNRate;

    private int mVisible, mUseful;

    private int mMessage;

    public GNSS() {
        GPS_ON = false;
        GLONASS_ON = false;

        mVisibleGNSS = new ArrayList<>();
        mUsefulGNSS = new ArrayList<>();
        mGNSSPotision = new ArrayList<>();
        mGNSSSNRate = new ArrayList<>();

        mVisible = 0;
        mUseful = 0;
        mMessage = 0;
    }

    public void setGPS(String[] rNMEA) {
        GLONASS_ON = false;

        if(rNMEA[6].charAt(0) == 'N')
            GPS_ON = false;
        else
            GPS_ON = true;
    }

    public void setGLONASS(String[] rNMEA) {
        GPS_ON = false;

        if(rNMEA[6].charAt(1) == 'N')
            GLONASS_ON = false;
        else
            GLONASS_ON = true;
    }

    public void setGNSS(String[] rNMEA) {
        if(rNMEA[6].charAt(0) == 'N')
            GPS_ON = false;
        else
            GPS_ON = true;

        if(rNMEA[6].charAt(1) == 'N')
            GLONASS_ON = false;
        else
            GLONASS_ON = true;
    }

    public void setVisibleGNSS(String[] rNMEA) {
        mMessage = Integer.valueOf(rNMEA[2]);
        mVisible = Integer.valueOf(rNMEA[3]);

        if(mMessage == 1 && mVisibleGNSS != null) {
            mVisibleGNSS.clear();
            mGNSSPotision.clear();
            mGNSSSNRate.clear();
        }

        for(int i = 1; i <= 4; i++) {
            if(i*4+3 < rNMEA.length && !rNMEA[i*4].isEmpty() && !rNMEA[i*4+1].isEmpty()
                    && !rNMEA[i*4+2].isEmpty() && !rNMEA[i*4+3].isEmpty()) {
                mVisibleGNSS.add(Integer.valueOf(rNMEA[i*4]));
                mGNSSPotision.add(new SatelitePotision(Double.valueOf(rNMEA[i*4+1]), Double.valueOf(rNMEA[i*4+2])));
                mGNSSSNRate.add(Integer.valueOf(rNMEA[i*4+3]));
            }
        }
    }

    public void setUsefulGNSS(String[] rNMEA) {
        int count = 0;

        if(mUsefulGNSS != null) mUsefulGNSS.clear();

        for(int i = 0; i < 12; i++) {
            if(!rNMEA[i+3].isEmpty()) {
                mUsefulGNSS.add(Integer.valueOf(rNMEA[i + 3]));
                count++;
            }
        }

        mUseful = count;
    }

    public ArrayList<Integer> getVisibleGNSS() { return mVisibleGNSS; }

    public ArrayList<Integer> getUsefulGNSS() { return mUsefulGNSS; }

    public ArrayList<SatelitePotision> getPotision() { return mGNSSPotision; }

    public ArrayList<Integer> getSNRate() { return mGNSSSNRate; }

    public int getVisibleCount() { return mVisible; }

    public int getUsefulCount() { return mUseful; }
}
