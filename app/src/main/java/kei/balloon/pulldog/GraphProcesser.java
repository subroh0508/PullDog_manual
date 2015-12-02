package kei.balloon.pulldog;


import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class GraphProcesser
{
    //Objects
	private List<Edge> edge;
	private List<Vertex> vertex;
	private List<Landmark> landmark;
	private List<Edge> route;
    private List<Area> area;
    private List<Information> information;

	private List<PointD> routePointList;

	private LinkedList<Edge> queue;
	private LinkedList<Vertex> vQueue;
	private double totalDistance; 
	private final static double CONNECTION_DISTANCE = 0.000005 * 0.000005;
	private final static double LATITUDE_TO_METER = 111262.393;
	private final static double LONGITUDE_TO_METER = 91158.432; //東京での係数． 任意地点の近似だったら緯度の係数にcos掛ける. 正確には楕円モデルが必要．
	public final static double METER_OF_LATITUDE = 1.0 / LATITUDE_TO_METER;
    public final static double METER_OF_LONGITUDE = 1.0 / LONGITUDE_TO_METER;
    public final static double BLOCK_SIZE = 0.30;

	private Edge dummy;
    private Edge separetedEdge0, separetedEdge1, assistedEdge;
	private Landmark start, end, startTmp = null, relaySTmp = null, relayETmp = null;
	private int vRegistId = 0;
	private final static int ROUTE_OFFSET = 10000;
    private final static int TMP_LANDMARK = 2222;

	private final static String TAGLIST  = "ExperimentForKitano/TagList.txt";
	private final static String LINKLIST = "ExperimentForKitano/LinkList.csv";
	private final static String LANDMARKLIST = "ExperimentForKitano/LandmarkList.csv";

    private final static String INFORMATION = "ExperimentForKitano/Information.txt";

    private MainActivity context;


	public GraphProcesser(String... fileNames){
		this(null, fileNames);
	}

    public GraphProcesser(MainActivity ma, String... fileNames){
        context = ma;

        edge = new ArrayList<Edge>();
        vertex = new ArrayList<Vertex>();
        landmark = new ArrayList<Landmark>();
        area = new ArrayList<Area>();
        routePointList = new ArrayList<PointD>();
        information = new ArrayList<Information>();
        importData(fileNames);
        importVertexs();
        importInformations();
        connectionInit(); //高速化の余地あり
    }

	private void connectionInit(){
        for (Edge e : edge) e.clearLinkedObject();
        for (Landmark l : landmark) l.clearLinkedObject();

		for (Edge e1 : edge){
			for (Edge e2 : edge){
				if  (e1.getId() != e2.getId() && !e1.isExistInConnectionList(e2)){
					if  (Edge.getDistance(e1, e2) < CONNECTION_DISTANCE){
						e1.addLinkedObject(e2);
						e2.addLinkedObject(e1);
					}
				}
			}
		}
		for (Edge e : edge){
			for (Landmark l : landmark){
				if  (!l.isInDoor()){
					if  (Edge.getDistance(e, l) < CONNECTION_DISTANCE){
						e.addLinkedObject(l);
						l.addLinkedObject(e);
					}
				}
			}
		}
	}

    public void addInfomation(Information i) {
        information.add(i);
    }

    private void importInformations() {
        String line, info;
        String[] tmp;
        double lat, lng;
        int category, tagId, id;
        try {
            //入力ストリームの作成
            InputStream fis = context.getAssets().open(INFORMATION);
            InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                tmp = line.split(",");
                if (tmp.length == 6) {
                    id = Integer.parseInt(tmp[0]);
                    lat = Double.parseDouble(tmp[1]);
                    lng = Double.parseDouble(tmp[2]);
                    tagId = Integer.parseInt(tmp[3]);
                    info = tmp[4];
                    category = Integer.parseInt(tmp[5]);

                    information.add(new Information(id, lat, lng, tagId, info ,category));
                } else if  (tmp.length == 5) {
                    id = Integer.parseInt(tmp[0]);
                    lat = Double.parseDouble(tmp[1]);
                    lng = Double.parseDouble(tmp[2]);
                    info = tmp[3];
                    category = Integer.parseInt(tmp[4]);

                    information.add(new Information(id, lat, lng, info ,category));
                } else if (tmp.length == 4) {
                    id = Integer.parseInt(tmp[0]);
                    tagId = Integer.parseInt(tmp[1]);
                    info = tmp[2];
                    category = Integer.parseInt(tmp[3]);

                    information.add(new Information(id, tagId, info ,category));
                }
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

	private void importVertexs(){
		String line;
		String[] tmp;
		int uID, tagID;
		int id1, id2;
		double d;

		try {
            //入力ストリームの作成
            InputStream fis = context.getAssets().open(TAGLIST);
            InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		    BufferedReader br = new BufferedReader(isr);
			
			while ((line = br.readLine()) != null){
				tmp = line.split(",");
                if  (tmp.length == 2) {
                    uID = Integer.parseInt(tmp[0]);
                    tagID = Integer.parseInt(tmp[1]);
                    vertex.add(new Vertex(uID, tagID, Vertex.IN));
                } else if (tmp.length == 3){
                    uID = Integer.parseInt(tmp[0]);
                    double lat = Double.parseDouble(tmp[1]);
                    double lng = Double.parseDouble(tmp[2]);
                    vertex.add(new Vertex(uID, uID, new PointD(lng, lat),  Vertex.OUT));
                }
			}
			br.close();
		} catch(Exception e) {
		    e.printStackTrace();
		}

		try {
            // 入力ストリームの生成
		    //File inputFile = new File(LINKLIST);
		    //FileInputStream fis = new FileInputStream(inputFile);

            InputStream fis = context.getAssets().open(LINKLIST);
		    InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		    BufferedReader br = new BufferedReader(isr);

			while ((line = br.readLine()) != null){
				tmp = line.split(",");
				id1 = Integer.parseInt(tmp[0]);
				id2 = Integer.parseInt(tmp[1]);
				d   = Double.parseDouble(tmp[2]) * BLOCK_SIZE;
				Vertex v = getVertexByUId(id1);
				v.addLinkedVertex(getVertexByUId(id2), d);
			}
			br.close();
		} catch(Exception e) {
		    e.printStackTrace();
		}

		try {
            // 入力ストリームの生成
		    //File inputFile = new File(LANDMARKLIST);
		    //FileInputStream fis = new FileInputStream(inputFile);

            InputStream fis = context.getAssets().open(LANDMARKLIST);
		    InputStreamReader isr = new InputStreamReader(fis);
		    BufferedReader br = new BufferedReader(isr);

		    //読み込み
			while ((line = br.readLine()) != null){
				tmp = line.split(",");
				int uId = Integer.parseInt(tmp[0].substring(1, tmp[0].length()-1));
				String lName = tmp[1].substring(1, tmp[1].length()-1);
				boolean isLinker = Integer.parseInt(tmp[2].substring(1, tmp[2].length()-1)) == 1 ? true : false;
				landmark.add(new Landmark(uId, uId, lName, isLinker));
			}

			//Linkerの初期化
			for (Landmark l1 : this.getLinkerLandmarks()){
				for (Landmark l2 : this.getLinkerLandmarks()){
					if  (l1.getId() != l2.getId() && l1.getName().equals(l2.getName())){
						l1.setLinkedLandmark(l2);
					}
				}	
			}
			br.close();
		} catch(Exception e) {
		    e.printStackTrace();
		}
	}


	public void searchInit(){
		route = new ArrayList<Edge>();
		queue = new LinkedList<Edge>();
		totalDistance = 0.0;
		for (Edge e : edge) e.searchInit();
		dummy = new Edge(0, 0, 0, 0, Edge.DUMMY_ID, "dummy");
		dummy.visit();
		dummy.setDistanceToEnd(0);
	}

	private Vertex getVertexByUId(int idNum){
		for (Vertex v : vertex){
			if  (v.getId() == idNum) return v;
		}
		return null;
	}

	public Vertex getVertexByTagId(int idNum){
		for (Vertex v : vertex){
			if  (v.getTagId() == idNum) return v;
		}
		return null;
	}

	public static double toMeter(double x1, double y1, double x2, double y2){
		double dx = (x2 - x1) * LONGITUDE_TO_METER;
		double dy = (y2 - y1) * LATITUDE_TO_METER;
		return (double)Math.sqrt(dx*dx + dy*dy);
	}

	public static double toMeter(PointD p1, PointD p2){
		return toMeter(p1.x, p1.y, p2.x, p2.y);
	}

    public static double toMeter(LatLng l1, LatLng l2){
        return toMeter(l1.longitude, l1.latitude, l2.longitude, l2.latitude);
    }

	public void printVertexs(){
		for (Vertex v : vertex){
			v.printInformation();
		}
	}

    public List<Area> getArea(){
        return area;
    }

    public List<Edge> getEdge(){
        return edge;
    }

	private void importData(String[] fileNames){
		String line;
		String[] tmp1, tmp2, tmp3;
		int registNum = 0, registNum2 = 0;
		try {
			for (String inputFileName : fileNames){
                //入力ストリームの作成
			    //File inputFile = new File(inputFileName);
			    //FileInputStream fis = new FileInputStream(inputFile);

                InputStream fis = context.getAssets().open(inputFileName);
                InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			    BufferedReader br = new BufferedReader(isr);

			    //Edge or Landmark
			    while ((line = br.readLine()) != null){
			    	if  (line.indexOf("SafeArea") != -1) break;
			    	tmp1 = line.split(" ");
			    	if  (tmp1.length > 2){
				    	for (int i=0; i<tmp1.length-2; i++){
					    	tmp2 = tmp1[i].split(",");
					    	tmp3 = tmp1[i+1].split(",");
                            Log.d("GP", "i:"+i+" ("+tmp2[0]+","+tmp2[1]+"/"+tmp3[0]+","+tmp3[1]+")"+"["+tmp1[tmp1.length-1]+"]");
					    	edge.add(new Edge(Double.parseDouble(tmp2[0]), Double.parseDouble(tmp2[1]),
					    						Double.parseDouble(tmp3[0]), Double.parseDouble(tmp3[1]), registNum++, tmp1[tmp1.length-1]));
				    	}
				    } else {
				    	boolean isLinker = false;
				    	tmp2 = tmp1[0].split(",");
				    	if  (tmp1[1].indexOf("_l") != -1){
				    		isLinker = true;
				    		tmp1[1] = tmp1[1].substring(0, tmp1[1].length() - 2);
				    	}
				    	landmark.add(new Landmark(Double.parseDouble(tmp2[0]), Double.parseDouble(tmp2[1]), registNum2++, tmp1[1], isLinker));
				    }
			    }
			    //Area
			    if  (line != null){
				    if  (line.indexOf("SafeArea") != -1){
					    while ((line = br.readLine()) != null){
					    	List<PointD> tmpPoints = new ArrayList<PointD>(); 
					    	tmp1 = line.split(" ");
					    	for (int i=0; i<tmp1.length-1; i++){
						    	tmp2 = tmp1[i].split(",");
						    	PointD tmpPoint = new PointD(Double.parseDouble(tmp2[0]), Double.parseDouble(tmp2[1]));
						    	tmpPoints.add(tmpPoint);
					    	}
					    	area.add(new Area(tmpPoints, tmp1[tmp1.length-1]));
					    }
				    }
				}
			    br.close();
			    System.out.println("Imported " + "\"" + inputFileName + "\"");
			}
		} catch(Exception e) {
		    e.printStackTrace();
		}
		System.out.println("");
	}

	public void printAllEdges(){
		for (Edge e : edge) e.printVertexPoint();
	}

	public void printConnectionState(){
		for (Edge e : edge) e.printLinkedObjects();
		for (Landmark l : landmark) l.printLinkedObjects();
	}

	public void applyDijkstra(Vertex end){
		for (Vertex v : vertex){
			v.searchInit();
		}
		end.setDistanceToEnd(0.0);
		end.visit();
		vQueue = new LinkedList<Vertex>();
		vQueue.offer(end);

		while (!vQueue.isEmpty()){
			Vertex now = vQueue.poll();

			for (Distance d : now.getLinkedVertexs()){
				Vertex v = d.linkedVertex;
				double distance = d.distance;
				if  (!v.isVisited() || v.getDistanceToEnd() > now.getDistanceToEnd() + distance){
					v.setVertexToEnd(now);
					v.setDistanceToEnd(now.getDistanceToEnd() + distance);
					v.visit();
					vQueue.offer(v);
				}
			}
		}
	}

	private Landmark getLandmarkByTagId(int idNum){
		for (Landmark l : landmark){
			if  (l.getTagId() == idNum) return l;
		}
		return null;
	}

	public Route searchRoute(Vertex startV, Vertex endV){
		applyDijkstra(endV);

		List<Vertex> res = new ArrayList<Vertex>();
		Vertex now = startV;
		while (now.getId() != endV.getId()){
			res.add(now);
			now = now.getVertexToEnd();
		}
		res.add(endV);

        List<Information> info = new ArrayList<>();


        for (Information i : information){
            if  (i.isInDoor()){
                for (Vertex v : res){
                    if  (v.getTagId() == i.getTagId()){
                        info.add(i);
                        break;
                    }
                }
            }
        }
		return new Route(context, res, getLandmarkByTagId(startV.getTagId()), getLandmarkByTagId(endV.getTagId()), startV.getDistanceToEnd(), info, null);
	}

    public void clearInformationNotified(){
        for (Information i : information) i.clearNotified();
    }

	public List<Vertex> searchRouteWithoutDjikstra(Vertex startV, Vertex endV){

		List<Vertex> res = new ArrayList<Vertex>();
		Vertex now = startV;
		while (now.getId() != endV.getId()){
			res.add(now);
			now = now.getVertexToEnd();
		}
		res.add(endV);

		return res;
	}

	private List<Landmark> getLinkerLandmarks(){
		List<Landmark> res = new ArrayList<Landmark>();
		for (Landmark l : landmark){
			if  (l.isLinker()) res.add(l);
		}

		return res;
	}

	private List<Landmark> getAvailableLinkerLandmarks(){
		List<Landmark> res = new ArrayList<Landmark>();
		for (Landmark l : landmark){
			if  (l.isLinker() && l.getLinkedLandmark() != null) res.add(l);
		}

		return res;
	}

	private Landmark getLinkerLandmarkByName(String str, boolean isInDoor){
		for (Landmark l : getLinkerLandmarks()){
			if  (l.getName().equals(str) && l.isInDoor() == isInDoor) return l;
		}

		return null;
	}

    public Route searchRoute(LatLng startL, Landmark endL){
        if  (relaySTmp != null) landmark.remove(relaySTmp);
        relaySTmp = new Landmark(startL.longitude, startL.latitude, Landmark.RELAY_ID, "relayLocation", false);
        landmark.add(relaySTmp);
        connectionInit();
        return searchRoute(relaySTmp, endL);
    }

    public Route searchRoute( Landmark startL, LatLng endL){
        if  (relayETmp != null) landmark.remove(relayETmp);
        relayETmp = new Landmark(endL.longitude, endL.latitude, Landmark.RELAY_ID, "relayLocation", false);
        landmark.add(relayETmp);
        connectionInit();
        return searchRoute(startL, relayETmp);
    }

    public  Route searchRouteFromCurrentLocation(LatLng startL, LatLng endL){
        if  (relayETmp != null) landmark.remove(relayETmp);
        relayETmp = new Landmark(endL.longitude, endL.latitude, Landmark.RELAY_ID, "relayLocation", false);
        landmark.add(relayETmp);
        connectionInit();
        return searchRouteFromCurrentLocation(relayETmp, new PointD(startL.longitude, startL.latitude));
    }

    public Route searchRouteFromCurrentLocation(String destination, int currentId){
        Landmark endL = null;
        for (Landmark l : landmark){
            if  (l.getName().equals(destination) && !l.isLinker()) endL   = l;
        }
        if  (endL == null){
            System.out.println("[Error] " + "Depature or/and Destination dosen't exist.");
            return null;
        }

        return searchRouteFromCurrentLocation(endL, currentId);
    }

    public Route searchRouteFromCurrentLocation(LatLng endL, int currentId){
        if  (relayETmp != null) landmark.remove(relayETmp);
        relayETmp = new Landmark(endL.longitude, endL.latitude, Landmark.RELAY_ID, "relayLocation", false);
        landmark.add(relayETmp);
        connectionInit();

        return searchRouteFromCurrentLocation(relayETmp, currentId);
    }

    public Route searchRouteFromCurrentLocation(Landmark endL, int currentId){
        Landmark startL;
        startL = getLandmarkByTagId(currentId);

        if  (startL == null){
            if  (startTmp != null) landmark.remove(startTmp); //2回目以降

            startTmp = new Landmark(currentId, TMP_LANDMARK, "currentLocation", false);
            landmark.add(startTmp);
            connectionInit();
        } else {
            return searchRoute(startL, endL);
        }

        return searchRoute(startTmp, endL);
    }

    public Route searchRoute(Landmark startL, Landmark endL){
        List<Edge> oriEdge;
        start = startL;
        end   = endL;
        Landmark origStart = start;
        Landmark origEnd   = end;

        if  (!start.isInDoor() && !end.isInDoor()){
            searchInit();
            applyDijkstra();

            Edge now = start.getLinkedEdges().get(0);
            while (now.getId() != dummy.getId()){
                route.add(now);
                now = now.getEdgeToEnd();
            }
            totalDistance = start.getLinkedEdges().get(0).getDistanceToEnd();
            System.out.println("<Start> : " + start.getName());
            printRoute();
            System.out.println("<End>   : " + end.getName());

            edgeToVertex();
            return getRoute();
        } else if (start.isInDoor() && end.isInDoor()){
            int startId = start.getTagId();
            int endId   = end.getTagId();
            Route vRoute = searchRoute(getVertexByUId(startId), getVertexByUId(endId));

            return vRoute;
        } else {
            if  (start.isInDoor() && !end.isInDoor()){ //中→外
                searchInit();
                applyDijkstra();
                applyDijkstra(getVertexByUId(start.getTagId()));
            } else {											  //外→中
                end = start;
                searchInit();
                applyDijkstra();
                applyDijkstra(getVertexByUId(origEnd.getTagId()));
            }

            double minDistance = 999999999;
            String minName = new String();
            Landmark relayLandmark = null;

            for (Landmark l : getAvailableLinkerLandmarks()) l.searchInit();
            for (Landmark l : getAvailableLinkerLandmarks()){
                if  (!l.isChecked()){
                    Landmark innerLandmark = getLinkerLandmarkByName(l.getName(), true);
                    Landmark outerLandmark = getLinkerLandmarkByName(l.getName(), false);
                    innerLandmark.check();
                    outerLandmark.check();
                    double innerDistance = getVertexByTagId(innerLandmark.getTagId()).getDistanceToEnd();
                    double outerDistance = outerLandmark.getLinkedEdges().get(0).getDistanceToEnd();
                    if  (innerDistance + outerDistance < minDistance){
                        minDistance = innerDistance + outerDistance;
                        minName = l.getName();
                        start = outerLandmark;
                        relayLandmark = innerLandmark;
                    }
                }
            }
            Edge now = start.getLinkedEdges().get(0);
            while (now.getId() != dummy.getId()){
                route.add(now);
                now = now.getEdgeToEnd();
                if  (now == null){
                    Log.e("Route", "Route Not Found.");
                    Toast.makeText(context, "Route Not Found.", Toast.LENGTH_LONG).show();
                    return null;
                }
            }

            //originalEdges
            oriEdge = route;

            //pickUpInformation
            List<Information> iList = new ArrayList<Information>();
            for (Information i : information){
                if  (!i.isInDoor()) {
                    for (Edge e : oriEdge) {
                        if (GraphProcesser.getDistance(e, i) < Information.REGIST_DISTANCE) {
                            iList.add(i);
                            break;
                        }
                    }
                }
            }

            //setTotalDistance
            totalDistance = start.getLinkedEdges().get(0).getDistanceToEnd();
            edgeToVertex();

            //debug
            System.out.println(minName + "を通って，" + minDistance + "[m]");

            List<Vertex> mergedList = new ArrayList<Vertex>();

            if  (origStart.isInDoor() && !origEnd.isInDoor()){ //中→外
                List<Vertex> outerList = new ArrayList<Vertex>();
                if  (routePointList.size() > 0){
                    for (PointD p : routePointList){
                        while (isAlredayExist(outerList, vRegistId + ROUTE_OFFSET)) vRegistId++;
                        Vertex v = new Vertex(vRegistId + ROUTE_OFFSET, p, Vertex.OUT);
                        outerList.add(v);
                    }
                }
                List<Vertex> innerList = searchRouteWithoutDjikstra(getVertexByTagId(relayLandmark.getTagId()),
                        getVertexByTagId(origStart.getTagId()));
                Collections.reverse(innerList);

                mergedList.addAll(innerList);
                mergedList.addAll(outerList);
            } else { //外→中
                List<Vertex> outerList = new ArrayList<Vertex>();
                if  (routePointList.size() > 0){
                    for (PointD p : routePointList){
                        while (isAlredayExist(outerList, vRegistId + ROUTE_OFFSET)) vRegistId++;
                        Vertex v = new Vertex(vRegistId + ROUTE_OFFSET, p, Vertex.OUT);
                        outerList.add(v);
                    }
                }
                List<Vertex> innerList = searchRouteWithoutDjikstra(getVertexByTagId(relayLandmark.getTagId()),
                        getVertexByTagId(origEnd.getTagId()));
                Collections.reverse(outerList);
                Collections.reverse(oriEdge);
                mergedList.addAll(outerList);
                mergedList.addAll(innerList);
            }

            for (Information i : information){
                if  (i.isInDoor()){
                    for (Vertex v : mergedList){
                        if  (v.getTagId() == i.getTagId()) iList.add(i);
                    }
                }
            }

            return new Route(context, mergedList, origStart, origEnd, minDistance, iList, oriEdge);
        }
    }

	public Route searchRoute(String startName, String endName){
		Landmark startL = null;
		Landmark endL = null;

		for (Landmark l : landmark){
			if  (l.getName().equals(startName) && !l.isLinker()) startL = l;
			if  (l.getName().equals(endName  ) && !l.isLinker()) endL   = l;
		}
		if  (startL == null || endL == null){
			System.out.println("[Error] " + "Depature or/and Destination dosen't exist.");
			return null;
		}

        return searchRoute(startL, endL);
	}

    public Landmark getLandmarkByName(String lName){
        for (Landmark l : landmark){
            if  (l.getName().equals(lName)) return l;
        }
        return null;
    }

    public LinkedList<Landmark> getLandmarksByName(String lName){
        LinkedList<Landmark> res = new LinkedList<Landmark>();
        for (Landmark l : landmark){
            if  (l.getName().indexOf(lName) != -1 && !l.isLinker()) res.offer(l);
        }
        return res;
    }

    public static Edge getNearestEdge(List<Edge> eList, LatLng l){
        Edge res = null;
        PointD p = new PointD((double)l.longitude, (double)l.latitude);
        double minDistance = 999999;
        for (Edge e : eList){
            PointD minPoint = e.getMinimumPoint(p);
            double dis = Edge.getDistance(minPoint, p);
            if  (dis < minDistance){
                minDistance = dis;
                res = e;
            }
        }

        return res;
    }

    public static List<Edge> restorationEdges(List<Edge> originalEList, List<Vertex> vList){
        List<Edge> res = new ArrayList<Edge>();

        for (Vertex v : vList){
            if  (!v.isInDoor()){
                double minDistance = 9999999;
                Edge nearestEdge = null;
                PointD currentLocation = new PointD(v.getX(), v.getY());
                if  (vList.indexOf(v) < vList.size()-1){
                    Vertex vNext = vList.get(vList.indexOf(v) + 1);
                    if  (!vNext.isInDoor()){
                        PointD currentLocation2 = new PointD(vNext.getX(), vNext.getY());
                        for (Edge e : originalEList){
                            PointD p1 = e.getVertexsPoint()[0];
                            PointD p2 = e.getVertexsPoint()[1];

                            double distance1 = Edge.getDistance(p1, currentLocation) + Edge.getDistance(p2, currentLocation2);
                            double distance2 = Edge.getDistance(p2, currentLocation) + Edge.getDistance(p1, currentLocation2);
                            double distance = Math.min(distance1, distance2);
                            if  (distance < minDistance){
                                minDistance = distance;
                                nearestEdge = e;
                            }
                        }
                    }
                } else {
                    for (Edge e : originalEList){
                        PointD p = e.getMinimumPoint(currentLocation);
                        double distance = Edge.getDistance(p, currentLocation); //現在地から対象点字ブロックへの最小距離
                        if  (distance < minDistance){
                            minDistance = distance;
                            nearestEdge = e;
                        }
                    }
                }
                res.add(nearestEdge);
            }
        }
        return res;
    }

    public Route searchRouteFromCurrentLocation(String endName, PointD currentLocation) {
        start = new Landmark(currentLocation.x, currentLocation.y, Landmark.HERE_ID, "currentLocation", false);
        end = getLandmarkByName(endName);
        if  (end == null){
            System.out.println("[Error] " + endName + " dosen't exist.");
            Toast.makeText(context, "[Error] " + endName + " dosen't exist", Toast.LENGTH_SHORT).show();
            return null;
        }

        return searchRouteFromCurrentLocation(currentLocation);
    }

    public Route searchRouteFromCurrentLocation(Landmark endL, PointD currentLocation) {
        start = new Landmark(currentLocation.x, currentLocation.y, Landmark.HERE_ID, "currentLocation", false);
        end = endL;
        if  (end == null){
            System.out.println("[Error] End dosen't exist.");
            Toast.makeText(context, "[Error] End dosen't exist.", Toast.LENGTH_SHORT).show();
            return null;
        }
        return searchRouteFromCurrentLocation(currentLocation);
    }

	//現在地からランドマークへの案内
	public Route searchRouteFromCurrentLocation(PointD currentLocation){


		boolean inArea = false;
		for (Area a : area){
			if  (a.includes(currentLocation)) inArea = true;
		}
		if  (!inArea){
			System.out.println("[Error] Current location is not available.");
            Toast.makeText(context, "[Error] Current location is not available.", Toast.LENGTH_SHORT).show();
			return null;
		}

		List<Edge> availableEdge = new ArrayList<Edge>(); //案内可能点字ブロックリスト
		double minDistance;
		Edge nearestEdge = null; //現在地から一番近い点字ブロック
		PointD nearestPoint = null; //一番近い点字ブロックへ誘導する座標点
		Edge assistEdge = null; //誘導する際の経路
		boolean ok;

		for (Area a : area){
			for (Edge e : getEdgesInArea(a)) availableEdge.add(e);
		}
		while (!availableEdge.isEmpty()){ //点字ブロックへの誘導可否を判断
			minDistance = 9999999;
			nearestEdge = null;
			nearestPoint = null;
			assistEdge = null;
			ok = false;
			for (Edge e : availableEdge){
				PointD p = e.getMinimumPoint(currentLocation);
				double distance = Edge.getDistance(p, currentLocation); //現在地から対象点字ブロックへの最小距離
				if  (distance < minDistance){
					minDistance = distance;
					nearestEdge = e;
					nearestPoint = p;
				}
			}
			assistEdge = new Edge(currentLocation.x, currentLocation.y, nearestPoint.x, nearestPoint.y, Edge.ASSIST_ID, "assist");
			for (Area a : area){
				if  (a.includesCompletely(assistEdge)) ok = true;
			}
			if  (ok) break;
			availableEdge.remove(nearestEdge);
		}

		if  (availableEdge.isEmpty()){ //補助線を引けなければ諦める
			System.out.println("[Error] Current location can't reach route.");
            Toast.makeText(context, "[Error] Current location can't reach route.", Toast.LENGTH_SHORT).show();
			return null;
		}

		//補助ルート探索に成功したら補助線を引き、引かれた方の点字ブロックを分割
		Edge[] newEdge = nearestEdge.separate(nearestPoint);

        if  (separetedEdge0 != null) edge.remove(separetedEdge0);
        if  (separetedEdge1 != null) edge.remove(separetedEdge1);
        if  (assistedEdge != null) edge.remove(assistedEdge);
        separetedEdge0 = newEdge[0];
        separetedEdge1 = newEdge[1];
        assistedEdge = assistEdge;

        edge.add(newEdge[0]);
		edge.add(newEdge[1]);
		edge.add(assistEdge);

		connectionInit();

		searchInit();
		applyDijkstra();

		Edge now = assistEdge;

		while (now.getId() != dummy.getId()){
			route.add(now);
			now = now.getEdgeToEnd();
            if  (now == null){
                Log.e("ROUTE", "Route not found");
                Toast.makeText(context, "Route not found", Toast.LENGTH_SHORT).show();
                return null;
            }
		}
        totalDistance = route.get(0).getDistanceToEnd();

		System.out.println("<Start> : Here");
		printRoute();
		System.out.println("<End>   : " + end.getName());

		edgeToVertex();
		return getRoute();
	}

	public void applyDijkstra(){
		for (Edge e : end.getLinkedEdges()){
			e.setEdgeToEnd(dummy);
			e.visit();
			e.setDistanceToEnd(e.getDistance());
			queue.offer(e);
		}
		while (!queue.isEmpty()){
			Edge now = queue.poll();
			for (Edge e : now.getLinkedEdges()){
				if  (!e.isVisited() || e.getDistanceToEnd() > now.getDistanceToEnd() + e.getDistance()){
					e.setEdgeToEnd(now);
					e.setDistanceToEnd(now.getDistanceToEnd() + e.getDistance());
					e.visit();
					queue.offer(e);
				}
			}
		}
	}

	public void printRoute(){
		for (Edge r : route) System.out.println(r.getName());
	}

    public static Vertex getNearestVertex(List<Vertex> vList, LatLng l){
        double minDistance = 999999;
        Vertex minV = null;

        for (Vertex v : vList){
            if  (!v.isInDoor()) {
                double dis = GraphProcesser.getDistance(v.getLatLng(), l);
                if (dis < minDistance) {
                    minV = v;
                    minDistance = dis;
                }
            }
        }

        return minV;
    }

    public static double getDistance(LatLng l1, LatLng l2){
        return Edge.getDistance(new PointD((double)l1.latitude, (double)l1.longitude),
                                 new PointD((double)l2.latitude, (double)l2.longitude));
    }

    //[m]
    public static double getDistance(Edge e, Information i){
        if  (i.isInDoor()) return -1.0;

        PointD iP = new PointD(i.getLng(), i.getLat());
        PointD p = e.getMinimumPoint(iP);
        return GraphProcesser.toMeter(p, iP);
    }



	private void edgeToVertex(){
		PointD p1 = route.get(0).getVertexsPoint()[0];
		PointD p2 = route.get(0).getVertexsPoint()[1];
        routePointList.clear();
		routePointList.add(start.getPoint()); //start地点を登録
		if  (Edge.comparePoint(start.getPoint(), p1, p2) == Edge.V1){ //近い方を選択
			route.get(0).selectVertex(Edge.V1);
		} else {
			route.get(0).selectVertex(Edge.V2);
		}
		for (Edge e : route){
			routePointList.add(e.getUnSelectPoint());
			if  (route.indexOf(e) + 1 > route.size() - 1) break;
			Edge next = route.get(route.indexOf(e) + 1);
			PointD n1 = next.getVertexsPoint()[0];
			PointD n2 = next.getVertexsPoint()[1];
			if  (Edge.comparePoint(e.getUnSelectPoint(), n1, n2) == Edge.V1){
				next.selectVertex(Edge.V1);
			} else {
				next.selectVertex(Edge.V2);
			}
		}
		routePointList.add(end.getPoint()); //end地点を登録
	}

	public void outputRouteForKml(){
		KmlDecoder kd = new KmlDecoder();
		kd.toKml(routePointList, start, end, context);
	}

	public List<Edge> getEdgesInArea(Area a){
		List<Edge> res = new ArrayList<Edge>();
		boolean includeFlg;
		for (Edge e : edge){
			includeFlg = false;
			for (PointD p : e.getVertexsPoint()){
				if  (a.includes(p)) includeFlg = true;
			}
			if  (includeFlg) res.add(e);
		}
		return res;
	}

	public void printEdgesInArea(Area a){
		for (Edge e : getEdgesInArea(a)) System.out.println(e.getName());
	}

	private boolean isAlredayExist(List<Vertex> list, int idNum){
		for (Vertex v : list){
			if  (v.getId() == idNum) return true;
		}
		return false;
	}

	public Route getRoute(){
		List<Vertex> vList = new ArrayList<Vertex>();
		if  (routePointList.size() > 0){
			for (PointD p : routePointList){
				while (isAlredayExist(vList, vRegistId)) vRegistId++;
				Vertex v = new Vertex(vRegistId, p, Vertex.OUT);
				vList.add(v);
			}
		}

        List<Information> iList = new ArrayList<Information>();
        for (Information i : information){
            if  (!i.isInDoor()) {
                for (Edge e : route) {
                    if (GraphProcesser.getDistance(e, i) < Information.REGIST_DISTANCE) {
                        iList.add(i);
                        break;
                    }
                }
            }
        }

		Route r = new Route(context, vList, start, end, totalDistance, iList, GraphProcesser.restorationEdges(edge, vList));
		return r;
	}

    public void setContext(MainActivity ma){
        context = ma;
    }

    public List<Landmark> getAllLandmark(){
        List<Landmark> res = new ArrayList<>();
        for(Landmark l : landmark){
            if(!l.isLinker()) res.add(l);
        }
        return res;
    }

	public static void main(String[] args)
	{
		GraphProcesser gp = new GraphProcesser("ExperimentForKitano/KitanoLine.txt", "ExperimentForKitano/KitanoPoint.txt", 
													"ExperimentForKitano/ランドマーク.txt", "ExperimentForKitano/SafeArea.txt");
		gp.searchRoute("バス停1", "バス停2");
		//Route r = gp.searchRoute("南口1", "バス停2");
		//Route r = gp.searchRoute("バス停1", "改札");
		//r.printInfo(gp.edge);
		//gp.outputRouteForKml();
		//PointD current = new PointD(139.35259, 35.64505);
		//if  (gp.searchRouteFromCurrentLocation("バス停2", current)){;
		//	gp.outputRouteForKml();
		//}

		//gp.printVertexs();
		//List<Vertex> vRoute = gp.searchRoute(gp.getVertexByUId(92), gp.getVertexByUId(48));
		//System.out.println(gp.totalDistance);
	}
}
