package kei.balloon.pulldog;

/**
 * Created by kei on 2015/09/16.
 */
public class RouteData {
    private int id;
    private String name;
    private int distance;

    public RouteData(int i, String n, int d) {
        id = i;
        name = n;
        distance = d;

    }

    public int getid(){
        return id;
    }

    public String getName(){
        return name;
    }
    public int getDistance(){
        return distance;
    }
}
