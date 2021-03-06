package kei.balloon.pulldog;

import java.io.Serializable;

/**
 * Created by root on 15/09/04.
 */
public class SatelitePotision implements Serializable{
    private double mElevation;
    private double mAzimuth;

    public SatelitePotision(double e, double a) {
        mElevation = e;
        mAzimuth = a;
    }

    public void setPotision(double e, double a) {
        mElevation = e;
        mAzimuth = a;
    }

    public double getElevation() {
        return mElevation;
    }

    public double getAzimuth() { return mAzimuth; }

    public double[] getPotision() {
        double[] ret = {mElevation, mAzimuth};

        return ret;
    }
}
