package kei.balloon.pulldog;

/**
 * Created by kei on 2015/09/12.
 */
public class PointParameter {
    public final double latitude;
    public final double longitude;
    private String information;
    private boolean visited;
    private int infoType;

    public PointParameter(double lt, double lg, String info){
        latitude = lt;
        longitude = lg;
        information = info;
        visited = false;
        infoType = 0;
    }

    public PointParameter(double lt, double lg, String info, int type){
        latitude = lt;
        longitude = lg;
        information = info;
        visited = false;
        infoType = type;
    }

    public void setVisitedFlag(){ visited = true; }

    public void setInfoType(int n) { infoType = n; }

    public String getInformation(){
      return information;
    }

    public boolean isVisited(){
        return visited;
    }

    public int getInfoType() { return infoType; }

}
