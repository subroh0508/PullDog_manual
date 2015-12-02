package kei.balloon.pulldog;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kei on 2015/09/12.
 */
public class RouteInfoGoogle {
    private List<PointParameter> mRoute;
    private List<PointParameter> mCorners;
    private List<LatLng> mRouteLatLng;
    private List<LatLng> mCornersLatLng;

    public RouteInfoGoogle(){
        mRoute = new ArrayList<>();
        mRouteLatLng = new ArrayList<>();
        mCorners = new ArrayList<>();
        mCornersLatLng = new ArrayList<>();

    }

    public RouteInfoGoogle(List<List<HashMap<String, String>>> r){
        mRoute = new ArrayList<>();
        mRouteLatLng = new ArrayList<>();
        mCorners = new ArrayList<>();
        mCornersLatLng = new ArrayList<>();

        for(HashMap h :r.get(0)){
            double lat = Double.valueOf(h.get("lat").toString());
            double lng = Double.valueOf(h.get("lng").toString());
            String info;
            if(h.get("info") != null) {
                String str = h.get("info").toString();
                Matcher m = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>").matcher(str);
                info = m.replaceAll("");
                info = info.replaceAll("する", "、です。");
            }
            else
                info = null;

            mRoute.add(new PointParameter(lat, lng, info));
            mRouteLatLng.add(new LatLng(lat, lng));
            Log.d("TAG", "mRoute:"+new LatLng(lat, lng).toString());

            if(info != null) {
                mCorners.add(new PointParameter(lat, lng, info));
                mCornersLatLng.add(new LatLng(lat, lng));
            }
        }

    }

    public List<LatLng> getPointList() {
        List<LatLng> p = new ArrayList<>();
        for(PointParameter r : mRoute) {
            LatLng ll = new LatLng(r.latitude, r.longitude);
            p.add(ll);
        }

        return p;
    }

    public PointParameter getNearestCorner(double lat, double lng) {
        double mind = 100000.0;
        PointParameter p = null;

        for(PointParameter c : mCorners) {
            if(getDistance(c.latitude, c.longitude, lat, lng) < mind && !c.isVisited()) {
                mind = getDistance(c.latitude, c.longitude, lat, lng);
                p = c;
            }
        }

        return p;
    }

    public int getCorner(double lat, double lng) {
        int i = 0, n = -1;
        double mind = 10000.0;

        for(PointParameter c : mCorners) {
            if(mind > getDistance(lat, lng, c.latitude, c.longitude) && !c.isVisited()){
                mind = getDistance(lat, lng, c.latitude, c.longitude);
                n = i;
            }
            i++;
        }

        return n;
    }

    public double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double dlat = Math.abs(lat1 - lat2);
        double dlng = Math.abs(lng1 - lng2);

        dlat /= GraphProcesser.METER_OF_LATITUDE;
        dlng /= GraphProcesser.METER_OF_LONGITUDE;

        return Math.sqrt(dlat*dlat+dlng*dlng);
    }

    public double getDistance(LatLng p1, LatLng p2) {
        double dlat = Math.abs(p1.latitude - p2.latitude);
        double dlng = Math.abs(p1.longitude - p2.longitude);

        dlat /= GraphProcesser.METER_OF_LATITUDE;
        dlng /= GraphProcesser.METER_OF_LONGITUDE;

        return Math.sqrt(dlat*dlat+dlng*dlng);
    }

    public double getTotalDistance() {
        double total = 0.0;

        for(int i = 0; i < mRoute.size()-1; i++)
            total += getDistance(mRoute.get(i).latitude, mRoute.get(i).longitude, mRoute.get(i+1).latitude, mRoute.get(i+1).longitude);

        return total;
    }

    public double getTotalDistance(List<LatLng> points) {
        double total = 0.0;

        for(int i = 0; i < points.size()-1; i++)
            total += getDistance(points.get(i).latitude, points.get(i).longitude, points.get(i+1).latitude, points.get(i+1).longitude);

        return total;
    }

    public double getRemainDistance(double lat, double lng) {
        double total = 0.0;

        int n = getCorner(lat, lng);

        if(0 <= n && n < mRouteLatLng.size()-1) {
            double tmp = getDistance(new LatLng(lat, lng), mRouteLatLng.get(n)) + getDistance(mRouteLatLng.get(n), mRouteLatLng.get(n+1));
            if (tmp < getDistance(new LatLng(lat, lng), mRouteLatLng.get(n+1)))
                total += tmp;
            else
                total += getDistance(new LatLng(lat, lng), mRouteLatLng.get(n+1));
            for(int i = n+1; i < mRouteLatLng.size()-1; i++)
                total += getDistance(mRouteLatLng.get(i), mRouteLatLng.get(i+1));
        } else {
            n = mRouteLatLng.size()-1;
            total += getDistance(lat, lng, mRouteLatLng.get(n).latitude, mRouteLatLng.get(n).longitude);
        }

        return total;
    }
    public void setFlag(PointParameter n){
        mCorners.get(mCorners.indexOf(n)).setVisitedFlag();
    }
}