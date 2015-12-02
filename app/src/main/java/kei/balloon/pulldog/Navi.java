package kei.balloon.pulldog;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//TODO "KitanoPoint.txt"は抜いてるううう

public class Navi
{
    //Objects
    private PointD location; //現在地
    private int tag; //現在タグID
    private Vertex vertex; //現在頂点
    private double direction; //向いている方向[deg]
    private GraphProcesser gp; //グラフプロセッサ
    private MainActivity context;

    //InDoorFlag
    private boolean inDoor;

    //PositioningMode
    private boolean positioningMode = RFID;
    public final static boolean RFID = true;
    public final static boolean GPS = false;

    //RecordRoute
    private Landmark myStart, myEnd;
    private String myRouteName;
    private int routeRegistNum;
    private double myRouteDistance;
    private List<Vertex> vList;

    private boolean isRecording = false;
    private final static int DEPATURE_ID    = 1000;
    private final static int DESTINATINO_ID = 2000;

    //Route
    private Route currentRoute;
    private LatLng nearestLatLng;
    private String depatureName, destinationName;
    private Landmark start, end;
    private boolean isRouting = false;
    private boolean isHealthlyRoute = false;
    private String directionNotify = null;
    private final static double NAVI_FINISH_RANGE = 1.0;
    public final static String DIRECTION_ASSIST_MSG = "メートル先，";
    private final static double DESTINATION_NOTIFY_DISTANCE = 5.0;
    private final static double NOTIFY_DISTANCE_1 = 4.0;
    private String directionNotifyMsg;
    private LatLng tmpEndL;
    private List<Vertex> visitedList;

    //RESPONCE
    public static int NOP = 0;
    public static int ROUTE_FINISH = 1;
    public static int DIRECTION_NOTIFY = 2;
    public static int DESTINATION_NOTIFY = 3;

    //NOTIFY MODE
    public final static int NEW_MODE = 0;
    public final static int CONVENTIONAL_MODE = 1;
    private static int notifyMode = 0;

    //RFID
    private RfidManager rm;

    //Dbg
    private LatLng[] dbg;

    //Information(Route)
    List<Information> currentInformation;

    //Reference Point
    private LatLng referencePoint;
    private boolean referenceUpdateFlag = false;


    public Navi(MainActivity ma, PointD p){
        context = ma;
        gp = new GraphProcesser(ma, "ExperimentForKitano/KitanoLine.txt", "ExperimentForKitano/Landmark.txt", "ExperimentForKitano/SafeArea.txt");
        location = p;
        currentInformation = new ArrayList<Information>();
        rm = new RfidManager(ma, "ExperimentForKitano/TagList.txt");
        visitedList = new ArrayList<>();
    }

    public Navi(MainActivity ma){
        this(ma, null);
    }

    public void setCurrentLocation(PointD p){
        this.setCurrentLocation(p.y, p.x);
    }

    public int setCurrentLocation(double lat, double lng){
        if  (location == null) location = new PointD();
        location.y = lat;
        location.x = lng;
        inDoor = false;

        if  (currentRoute != null)
            vertex = GraphProcesser.getNearestVertex(currentRoute.getVertex(), new LatLng(lat, lng));

        if (isRouting && currentRoute != null) {
            Landmark tgt = currentRoute.getEndLandmark();
            if  (!inDoor && !tgt.isInDoor()) {
                if (GraphProcesser.toMeter(new LatLng(lat, lng), new LatLng(tgt.getLat(), tgt.getLng())) < NAVI_FINISH_RANGE) {
                    return ROUTE_FINISH;
                }
                updateInformationList(lat, lng);
                if  (0 <= getRemainingDistance() && getRemainingDistance() <= DESTINATION_NOTIFY_DISTANCE) return DESTINATION_NOTIFY;
            } else {
                //中なら終了処理どうする？
                //タグIDの一致かな～
            }
        }

        return NOP;
    }

    public int setCurrentLocation(double lat, double lng, int tagId){
        if  (location == null) location = new PointD();
        location.y = lat;
        location.x = lng;
        inDoor = false;


        if  (currentRoute != null) vertex = GraphProcesser.getNearestVertex(currentRoute.getVertex(), new LatLng(lat, lng));
        else return NOP;

        if (isRouting) {
            Landmark tgt = currentRoute.getEndLandmark();
            if  (!inDoor && !tgt.isInDoor()) {
                //Toast.makeText(context, "残り : " + getRemainingDistance() + "| " + GraphProcesser.toMeter(new LatLng(lat, lng), new LatLng(tgt.getLat(), tgt.getLng())), Toast.LENGTH_SHORT).show();

                if (GraphProcesser.toMeter(new LatLng(lat, lng), new LatLng(tgt.getLat(), tgt.getLng())) < NAVI_FINISH_RANGE) {
                    return ROUTE_FINISH;
                }

                updateInformationList(lat, lng, tagId);

                if  (0 <= getRemainingDistance() && getRemainingDistance() <= DESTINATION_NOTIFY_DISTANCE) return DESTINATION_NOTIFY;
            }
        }

        return NOP;
    }

    public void enableRfid(){
        positioningMode = RFID;
    }

    public void enableGps(){
        positioningMode = GPS;
    }

    public void enableNewMode(){
        notifyMode = NEW_MODE;
    }

    public void enableConventionalMode(){
        notifyMode = CONVENTIONAL_MODE;
    }

    public int getNotifyMode(){
        return  notifyMode;
    }

    public void setReferencePoint(double lat, double lng){
        referencePoint = new LatLng(lat, lng);
        referenceUpdateFlag = false;
    }

    public LatLng getReferencePoint(){
        if  (location == null){
            Toast.makeText(context, "Location is not setted.", Toast.LENGTH_SHORT).show();
            return new LatLng(0.0, 0.0);
        }
        if  (referencePoint == null) return new LatLng(location.y, location.x);
        return referencePoint;
    }

    public void updateReferencePoint(){
        referenceUpdateFlag = true;
    }

    public boolean getReferenceUpdateFlag(){
        return referenceUpdateFlag;
    }

    public boolean getPositioningMode(){
        return positioningMode;
    }

    public void addInformation(Information i) {
        gp.addInfomation(i);
    }

    public boolean isCurrentRouteEndInDoor(){
        if  (currentRoute == null) return false;
        return currentRoute.getEndLandmark().isInDoor();
    }

    public int setCurrentLocation(int tagId){
        Rfid tag = rm.getRfidById(tagId);

        if  (tag == null){
            Log.e("NULL", "tagId:"+tagId+"...this tagNum dosen't exist.");
            return NOP;
        }

        //if  (tagId != this.tag) Toast.makeText(context, "Setted to " + Integer.toString(tagId), Toast.LENGTH_SHORT).show();

        this.tag = tagId;

        if(tag.isInDoor()){
            vertex = gp.getVertexByTagId(tagId);
            location = null;
            inDoor = true;

            if  (currentRoute == null){
                return NOP;
            }

            Toast.makeText(context, "Setted to " + Integer.toString(tagId), Toast.LENGTH_SHORT).show();

            if  (tagId == currentRoute.getEndLandmark().getTagId()){
                //Toast.makeText(context, "ROUTE_FINISH", Toast.LENGTH_SHORT).show();
                currentInformation.clear();
                return ROUTE_FINISH;
            }

            //情報更新
            updateInformationList(this.tag);

            if  (0 <= getRemainingDistance() && getRemainingDistance() <= DESTINATION_NOTIFY_DISTANCE) return DESTINATION_NOTIFY;


            switch (notifyMode){
                case CONVENTIONAL_MODE:
                    if  (getCurrentDirection().equals("左です") || getCurrentDirection().equals("右です")){
                        directionNotify = getCurrentDirection();
                        setDirectionNotifyMsg(directionNotify);
                        //Toast.makeText(context, "DIRECTION", Toast.LENGTH_SHORT).show();
                        return DIRECTION_NOTIFY;
                    }

                    int i = 1;
                    while(!getDirectionTo(i).equals("左です") && !getDirectionTo(i).equals("右です") && !getDirectionTo(i).equals("")){
                        i++;
                        int now = currentRoute.getVertex().indexOf(vertex);
                        if(i+now >= currentRoute.getVertex().size() || !currentRoute.getVertex().get(i+now).isInDoor()) break;
                    }

                    int index;
                    if  (currentRoute.getVertex().indexOf(vertex) + i >= currentRoute.getVertex().size()) index = currentRoute.getVertex().indexOf(vertex) + i - 1;
                    else index = currentRoute.getVertex().indexOf(vertex) + i;
                    Vertex corner = currentRoute.getVertex().get(index);

                    int d = (int)(Math.abs(vertex.getDistanceToEnd() - corner.getDistanceToEnd())); //ししゃごにゅう
                    //Toast.makeText(context, "d:"+Math.abs(vertex.getDistanceToEnd() - corner.getDistanceToEnd()), Toast.LENGTH_SHORT).show();

                    //TODO
                    if(1 <= d && d <= (int)NOTIFY_DISTANCE_1) {
                        directionNotify = getDirectionTo(i);
                        if (directionNotify.equals("")) return NOP;
                        setDirectionNotifyMsg(String.valueOf(d) + DIRECTION_ASSIST_MSG + directionNotify);
                        return DIRECTION_NOTIFY;
                    } else if((int)NOTIFY_DISTANCE_1 < d) {
                        setDirectionNotifyMsg("直線が"+String.valueOf(d)+"メートル続きます");
                        return DIRECTION_NOTIFY;
                    }
                    break;


                case NEW_MODE:

                    int j = 1;
                    while(!getDirectionTo(j).equals("左です") && !getDirectionTo(j).equals("右です") && !getDirectionTo(j).equals("")){
                        j++;
                        int now = currentRoute.getVertex().indexOf(vertex);
                        if(j+now >= currentRoute.getVertex().size() || !currentRoute.getVertex().get(j+now).isInDoor()) break;
                    }

                    int index2;
                    if  (currentRoute.getVertex().indexOf(vertex) + j >= currentRoute.getVertex().size()) index2 = currentRoute.getVertex().indexOf(vertex) + j - 1;
                    else index2 = currentRoute.getVertex().indexOf(vertex) + j;
                    Vertex corner2 = currentRoute.getVertex().get(index2);

                    //Toast.makeText(context, "index2 = " + index2, Toast.LENGTH_SHORT).show();

                    int d2 = (int)(Math.abs(vertex.getDistanceToEnd() - corner2.getDistanceToEnd())); //ししゃごにゅう

                    //Toast.makeText(context, "d = "+Math.abs(vertex.getDistanceToEnd() - corner2.getDistanceToEnd()) + ", d2 = " + d2, Toast.LENGTH_SHORT).show();

                    //TODO
                    if(1 <= d2 && d2 <= (int)NOTIFY_DISTANCE_1) {
                        directionNotify = getDirectionTo(j);
                        //Toast.makeText(context, "directionNotify = " + directionNotify, Toast.LENGTH_SHORT).show();
                        if (directionNotify.equals("")) return NOP;
                        setDirectionNotifyMsg("まもなく、" + directionNotify);
                        return DIRECTION_NOTIFY;
                    } else if((int)NOTIFY_DISTANCE_1 < d2 && d2 <= 15) {
                        directionNotify = getDirectionTo(j);
                        if (directionNotify.equals("")) return NOP;
                        setDirectionNotifyMsg(String.valueOf(d2) + DIRECTION_ASSIST_MSG + directionNotify);
                        return DIRECTION_NOTIFY;
                    } else if  (15 < d2 && d2 < 100){
                        setDirectionNotifyMsg("直線が"+String.valueOf(d2)+"メートル続きます");
                        return DIRECTION_NOTIFY;
                    }
                    break;

                default:
                    break;
            }



        }else {
            //Toast.makeText(context,"1",Toast.LENGTH_SHORT).show();
            Toast.makeText(context, "Setted to " + Integer.toString(tagId), Toast.LENGTH_SHORT).show();
            return setCurrentLocation(tag.getLat(), tag.getLng(), tagId);
        }


        return NOP;
    }

    public int getVisitedSize(){
        if  (visitedList == null) return -1;
        return visitedList.size();
    }

    private void setDirectionNotifyMsg(String msg){
        directionNotifyMsg = msg;
    }

    public String getDirectionNotify(){
        return directionNotifyMsg;
    }

    public boolean isExist(int tagId){
        return rm.isExist(tagId);
    }

    public LatLng getLatLng(){
        return new LatLng(location.y, location.x);
    }

    public boolean isExistInRoute(int tagId){
        if  (currentRoute == null) return false;
        for (Vertex v : currentRoute.getVertex()){
            if  (v.getTagId() == tagId) return true;
        }

        return false;
    }

    public double getRemainingDistance(){
        if  (currentRoute == null) return -1.0;
        if  (currentRoute.getStartLandmark().getTagId() == this.tag) return currentRoute.getTotalDistance();
        if  (inDoor) return currentRoute.getTotalDistance() - currentRoute.getDistanceTo(this.tag);
        else return currentRoute.getTotalDistance() - currentRoute.getDistanceTo(new LatLng(location.y, location.x));
    }

    public String getRemainingDistanceMsg(){
        int distance = (int)this.getRemainingDistance();
        int digit = 0;
        String msg = new String();
        String[] unit = {"", "十", "百", "千"};
        String meter = "メートル";

        if  (distance < 1 ) return "1メートル未満";
        if  (distance == 1) return "1メートル";

        int tmpD = 1;
        while ((distance / tmpD) > 0){
            tmpD *= 10;
            digit++;
        }

        for (int i = digit-1; i >= 0; i--){
            int div = (int)Math.pow(10, i);
            int tmp = distance / div;
            distance = distance % div;
            if  (tmp > 1) msg += Integer.toString(tmp) + unit[i];
            else if (tmp > 0) msg += unit[i];
        }

        return msg + meter;
    }

    private void updateInformationList(double lat, double lng){
        currentInformation.clear();
        if(currentRoute == null) return;
        for (Information i : currentRoute.getInformation()){
            if  (!i.isNotified()) {
                if (!i.isInDoor()) {
                    double dis = GraphProcesser.toMeter(i.getLatLng(), new LatLng(lat, lng));
                    if (dis < Information.NOTIFY_DISTANCE) currentInformation.add(i);
                }
            }
        }
    }

    private void updateInformationList(double lat, double lng, int tagIdNum){
        currentInformation.clear();
        if(currentRoute == null) return;
        for (Information i : currentRoute.getInformation()){
            if  (!i.isNotified()) {
                if (!i.isInDoor()) {
                    double dis = GraphProcesser.toMeter(i.getLatLng(), new LatLng(lat, lng));
                    if (dis < Information.NOTIFY_DISTANCE || tagIdNum == i.getTagId()) currentInformation.add(i);
                } else {
                    if  (tagIdNum == i.getTagId()) currentInformation.add(i);
                }
            }
        }
    }

    private void updateInformationList(int tagIdNum){
        currentInformation.clear();
        if(currentRoute == null) return;
        for (Information i : currentRoute.getInformation()){
            if  (!i.isNotified()) {
                if (i.isInDoor()) {
                    if  (tagIdNum == i.getTagId()) currentInformation.add(i);
                }
            }
        }
        //Toast.makeText(context, "size : " + Integer.toString(currentInformation.size()), Toast.LENGTH_SHORT).show();
    }

    public double getDistanceToNearestEdge(){
        if(currentRoute == null || inDoor) return -1.0;
        LatLng currentLocation = new LatLng(location.y, location.x);

        //List<Edge> routeEdge = GraphProcesser.restorationEdges(gp.getEdge(), currentRoute.getVertex());
        List<Edge> routeEdge = currentRoute.getEdge();

        Edge nearestEdge = GraphProcesser.getNearestEdge(routeEdge, currentLocation);
        PointD nearest = nearestEdge.getMinimumPoint(location);

        nearestLatLng = new LatLng(nearest.y, nearest.x);

        dbg = new LatLng[2];
        dbg[0] = new LatLng(nearest.y, nearest.x);
        dbg[1] = new LatLng(location.y, location.x);
        return GraphProcesser.toMeter(nearest, location);
    }

    public LatLng[] getLatlngForDebug(){
        return dbg;
    }

    public LatLng getLatLngToNearestEdge(){
        return nearestLatLng;
    }

    public List<Information> getNotify(){
        if(currentRoute == null) return currentInformation;
        for (Information i : currentInformation){
            for(Information i2 : currentRoute.getInformationById(i.getId())){ //同一IDをすべて通知済みにする。
                i2.notified();
            }
        }
        return currentInformation;
    }

    public void setDirection(double d){
        direction = d;
    }

    public Route generateRoute(String destination){
        /*
        if  (location == null){
            System.out.println("Current location is not available.");
            Toast.makeText(context, "Current location is not available.", Toast.LENGTH_SHORT).show();
            return null;
        }
        */

        if  (isRouting) {
            if  (!isCurrentRouteEndInDoor()){
                if  (inDoor) currentRoute = gp.searchRouteFromCurrentLocation(currentRoute.getEndLandmark(), this.tag);
                else currentRoute = gp.searchRouteFromCurrentLocation(currentRoute.getEndLandmark(), location);
            } else {
                if  (inDoor) currentRoute = gp.searchRouteFromCurrentLocation(currentRoute.getEndLandmark(), this.tag);
                else currentRoute = null;//仕様外ｗｗｗｗｗｗｗｗｗ
            }
        } else {
            if  (inDoor) currentRoute = gp.searchRouteFromCurrentLocation(destination, this.tag);
            else currentRoute = gp.searchRouteFromCurrentLocation(destination, location);
        }

        return currentRoute;
    }

    public Route generateRoute(String departure, String destination){
        currentRoute = gp.searchRoute(departure, destination);
        return currentRoute;
    }

    public  Route generateRoute(LatLng startL, Landmark endL){
        currentRoute = gp.searchRoute(startL, endL);
        return currentRoute;
    }

    public  Route generateRoute( Landmark startL, LatLng endL){
        currentRoute = gp.searchRoute(startL, endL);
        tmpEndL = endL;
        return currentRoute;
    }

    public Route getCurrentRoute(){
        return currentRoute;
    }

    public  Route generateRouteFromCurrentLocation(LatLng endL){
        if  (!inDoor) {
            currentRoute = gp.searchRouteFromCurrentLocation(new LatLng(location.y, location.x), endL);
        } else {
            currentRoute = gp.searchRouteFromCurrentLocation(endL, tag);
        }
        tmpEndL = endL;
        return currentRoute;
    }

    public Landmark getLandmarkByName(String lName){
        return gp.getLandmarkByName(lName);
    }

    public LinkedList<Landmark> getLandmarksByName(String lName){
        return gp.getLandmarksByName(lName);
    }

    public List<ArrayList<LatLng>> getAreaVertex(){
        List<ArrayList<LatLng>> res = new ArrayList<ArrayList<LatLng>>();
        for (Area a : gp.getArea()){
            res.add((ArrayList<LatLng>)a.getVertex());
        }

        return res;
    }

    public Vertex getNextDirectionVertex(){
        int n = currentRoute.getVertex().indexOf(vertex);

        int i = n;
        while(currentRoute.getNextDirection(i).equals("")){
            Log.d("TAG", currentRoute.getNextDirection(i));
            i++;
            if(currentRoute.getVertex().size() <= i) return null;
        }

        return currentRoute.getVertex().get(i);
    }

    public Vertex getCurrentVertex(){
        return vertex;
    }

    public boolean recordRoute(String routeName){
        if  (!inDoor && location != null) myStart = new Landmark(location.x, location.y, DEPATURE_ID, "Depature", false);
        else {
            Toast.makeText(context, context.getString(R.string.recoding_error) ,Toast.LENGTH_LONG).show();  //利用者目線
            return false;
        }

        myRouteName = routeName;
        routeRegistNum = 0;
        myRouteDistance = 0.0;
        vList = new ArrayList<Vertex>();
        vList.add( new Vertex(routeRegistNum++, -1, new PointD(location.x, location.y), false) );

        isRecording = true;
        return true;
    }

    public void setRouteAndStartRouting(Route rt){
        currentRoute = rt;
        this.startRouting();
        isHealthlyRoute = true;
    }

    public void visit(int tagId){
        Vertex v = gp.getVertexByTagId(tagId);
        if  (v != null) visitedList.add(v);
    }

    public boolean isVisited(int tagId){
        for (Vertex v : visitedList){
            if  (v.getTagId() == tagId) return true;
        }
        return false;
    }

    public boolean isHealthlyRoute(){
        return  isHealthlyRoute;
    }

    public void insertRoute(double lat, double lng){
        vList.add( new Vertex(routeRegistNum++, -1, new PointD((double)lng, (double)lat), false));
        myRouteDistance += GraphProcesser.toMeter(vList.get(vList.size()-2).getPoint(), vList.get(vList.size()-1).getPoint());
    }

    public Route finalizeRoute(){
        vList.add( new Vertex(routeRegistNum, -1, new PointD(location.x, location.y), false));
        myRouteDistance += GraphProcesser.toMeter(vList.get(vList.size()-2).getPoint(), vList.get(vList.size()-1).getPoint());
        myEnd = new Landmark(location.x, location.y, DESTINATINO_ID, "Destination", false);

        List<Edge> eList = new ArrayList<Edge>();
        int registNum = 0;
        for (Vertex v : vList){
            if  (vList.indexOf(v) ==  vList.size()-1) break;
            eList.add(new Edge(v.getLatLng(), vList.get(vList.indexOf(v)+1).getLatLng(), registNum++, "originalRoute"));
        }

        isRecording = false;
        return new Route(vList, myStart, myEnd, myRouteDistance, null, eList);
    }

    public boolean isRecording(){
        return isRecording;
    }

    public String getNextDirection(){
        if  (currentRoute == null) return "";
        return currentRoute.getNextDirection(currentRoute.getVertex().indexOf(vertex));
    }

    public String getCurrentDirection(){
        if  (currentRoute == null || currentRoute.getVertex().indexOf(vertex) <= 0) return "";
        return currentRoute.getNextDirection(currentRoute.getVertex().indexOf(vertex) - 1);
    }

    private String getDirectionTo(int num){
        if  (num == 0) return getCurrentDirection();
        if  (currentRoute == null) return "";
        return currentRoute.getNextDirection(currentRoute.getVertex().indexOf(vertex) - 1 + num);
    }

    public boolean isInDoor(){
        return inDoor;
    }

    public boolean isInSafeArea(LatLng l){
        for (Area a : gp.getArea()){
            if  (a.includes(l)) return true;
        }
        return false;
    }

    public boolean isInSafeArea(double lat, double lng){
        return this.isInSafeArea(new LatLng(lat, lng));
    }

    public void setRouteLandmarkName(String depature, String destination){
        depatureName = depature;
        destinationName = destination;

        end = gp.getLandmarkByName(destinationName);
    }

    public String getDepatureName(){
        if   (depatureName == null) return "NULUPO!";
        else return depatureName;
    }

    public String getDestinationName(){
        if   (destinationName == null) return "NULUPO!";
        else return destinationName;
    }

    public void startRouting(){
        if  (currentRoute == null){
            isRouting = false;
            Toast.makeText(context, "Route dosen't exist.", Toast.LENGTH_LONG).show();
        }
        isRouting = true;

        visitedList.clear();
        gp.clearInformationNotified();

        //TODO 本番どうする？
        this.enableRfid();
    }

    public void finishRouting(){
        isRouting = false;
        isHealthlyRoute = false;
    }

    public boolean isRouting(){
        return isRouting;
    }

    public List<Landmark> getAllLandmark(){
        return gp.getAllLandmark();
    }

    public List<Edge> getAllEdge(){
        return gp.getEdge();
    }

    public static void main(String[] args){

        Navi nv = new Navi(null);
        nv.setCurrentLocation(new PointD(139.35259, 35.64505));
        Route r = nv.generateRoute("バス停1","バス停2");
        r.printDirections();

    }
}

