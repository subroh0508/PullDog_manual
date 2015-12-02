package kei.balloon.pulldog;

import java.util.ArrayList;

/**
 * Created by root on 15/09/04.
 */
public class QZSS extends GNSS {
    public boolean QZSS_ON, L1SAIF_ON;

    private final static int QZSS_NUM[] = {193}, L1SAIF_NUM[] = {55};

    private ArrayList<Integer> mVisibleQZSS;
    private ArrayList<Integer> mUsefulQZSS;
    private ArrayList<SatelitePotision> mQZSSPotision;
    private ArrayList<Integer> mQZSSSNRate;
    private int mUsefulL1SAIF;
    private int mL1SAIFSNRate;

    private int mMessage;

    public QZSS() {
        super();

        mVisibleQZSS = new ArrayList<>();
        mUsefulQZSS = new ArrayList<>();
        mQZSSPotision = new ArrayList<>();
        mQZSSSNRate = new ArrayList<>();

        QZSS_ON = false;
        L1SAIF_ON = false;
        mUsefulL1SAIF = 0;
        mL1SAIFSNRate = 0;
    }

    public void checkQZSS() {
        if(mUsefulQZSS != null) mUsefulQZSS.clear();

        if(super.getUsefulGNSS() != null) {
            ArrayList<Integer> satelites = new ArrayList<>(super.getUsefulGNSS());
            int count = 0;

            for (Integer s : satelites) {
                for (int n : QZSS_NUM) {
                    if (s.equals(n)) {
                        mUsefulQZSS.add(n);
                        count++;
                    }
                }
            }

            if (count > 0) QZSS_ON = true;
            else QZSS_ON = false;
        }
    }

    public void setVisibleQZSS(String[] rNMEA) {
        mMessage = Integer.valueOf(rNMEA[2]);

        if(mVisibleQZSS != null) {
            mVisibleQZSS.clear();
            mQZSSPotision.clear();
            mQZSSSNRate.clear();
            mUsefulL1SAIF = 0;
            mL1SAIFSNRate = 0;
        }

        for(int i = 1; i <= 4; i++) {
            if(!rNMEA[i*4].isEmpty() && !rNMEA[i*4+3].isEmpty()) {
                if(!rNMEA[i*4+1].isEmpty() && !rNMEA[i*4+2].isEmpty()) {
                    mVisibleQZSS.add(Integer.valueOf(rNMEA[i*4]));
                    mQZSSPotision.add(new SatelitePotision(Double.valueOf(rNMEA[i*4+1]), Double.valueOf(rNMEA[i*4+2])));
                    mQZSSSNRate.add(Integer.valueOf(rNMEA[i*4+3]));
                } else if (rNMEA[i*4+1].isEmpty() && rNMEA[i*4+2].isEmpty()){
                    mUsefulL1SAIF = Integer.valueOf(rNMEA[i*4]);
                    mL1SAIFSNRate = Integer.valueOf(rNMEA[i*4+3]);
                }
            }
        }
    }

    public void checkL1SAIF() {
        L1SAIF_ON = false;

        for(int n : L1SAIF_NUM) {
            if(mUsefulL1SAIF == n) {
                L1SAIF_ON = true;
                break;
            }
        }
    }

    public ArrayList<Integer> getVisibleQZSS() { return mVisibleQZSS; }

    public ArrayList<Integer> getUsefulQZSS() { return mUsefulQZSS; }

    public ArrayList<SatelitePotision> getQZSSPotision() { return mQZSSPotision; }

    public ArrayList<Integer> getQZSSSNRate() { return mQZSSSNRate; }

    public int getUsefulL1SAIF() { return mUsefulL1SAIF; }

    public int getL1SAIFSNRate() { return mL1SAIFSNRate; }

    public SatelitePotision searchQZSSPotision(int id) { return mQZSSPotision.get(mVisibleQZSS.indexOf(id)); }
}
