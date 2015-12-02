package kei.balloon.pulldog;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Edge
{
	private PointD v1, v2;
	private boolean v1Select, v2Select;
	private double distance, distanceToEnd, distanceForCal;
	private int id;
	private String name;
	private List<Edge> linkedEdges;
	private List<Landmark> linkedLandmarks;
	private boolean visited;
	private Edge toEnd;
	public final static int V1 = 0;
	public final static int V2 = 1;
	private final static double END_POINT_X = 1000.0;
	private final static double END_POINT_Y = 100.0;
	public final static int ASSIST_ID = 10000;
	public final static int SEPARATED_EDGE_OFFSET = 100000;
	public final static int DUMMY_ID = -9999;

	public Edge(double x1, double y1, double x2, double y2, int idNum, String str){
		v1 = new PointD(x1, y1);
		v2 = new PointD(x2, y2);
		distance = GraphProcesser.toMeter(v1.x, v1.y, v2.x, v2.y);
        distanceForCal = (double)Math.sqrt(Edge.getDistance(new PointD(v1.x, v1.y), new PointD(v2.x, v2.y)));
		id = idNum;
		name = str;
		linkedEdges = new ArrayList<Edge>();
		linkedLandmarks = new ArrayList<Landmark>();
		visited = false;
		distanceToEnd = 9999;
		v1Select = false;
		v2Select = false;
	}

    public Edge(LatLng l1, LatLng l2, int idNum, String str){
        this(l1.longitude, l1.latitude, l2.longitude, l2.latitude, idNum, str);
    }

	public Edge(){
		System.out.println("Compile Test.");
	}

	public void printVertexPoint(){
		System.out.println("(" + v1.x + ", " + v1.y + ") --- (" + v2.x + ", " + v2.y + ")");
	}

	public void printLinkedObjects(){
		System.out.println("[" + name + "]");
		for (Edge e : linkedEdges) System.out.print(e.getName() + " ");
		for (Landmark l : linkedLandmarks) System.out.print("<" + l.getName() + "> ");
		System.out.println("");
		System.out.println("");
	}

	public void addLinkedObject(Edge e){
		linkedEdges.add(e);
	}

	public void addLinkedObject(Landmark l){
		linkedLandmarks.add(l);
	}

    public void clearLinkedObject(){
        linkedEdges.clear();
        linkedLandmarks.clear();
    }

	public int getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public PointD[] getVertexsPoint(){
		PointD[] res = new PointD[2];
		res[0] = v1;
		res[1] = v2;
		return res;
	}

    public LatLng[] getVertexsLatLng(){
        LatLng[] res = new LatLng[2];
        res[0] = new LatLng(v1.y, v1.x);
        res[1] = new LatLng(v2.y, v2.x);
        return res;
    }

	public boolean isExistInConnectionList(Edge e){
		for (Edge e1 : linkedEdges){
			if  (e1.getId() == e.getId()) return true;
		}
		return false;
	}

	public static double getDistance(Edge e1, Edge e2){
		double min = 9999;
		for (PointD eTmp1 : e1.getVertexsPoint()){
			for (PointD eTmp2 : e2.getVertexsPoint()){
				double dis = (eTmp2.x - eTmp1.x) * (eTmp2.x - eTmp1.x) + (eTmp2.y - eTmp1.y) * (eTmp2.y - eTmp1.y);
				if  (dis < min) min = dis;
			}
		}
		return min;
	}

    public LatLng getNearerLatLng(LatLng l){
        double dis1 = GraphProcesser.getDistance(l, this.getVertexsLatLng()[0]);
        double dis2 = GraphProcesser.getDistance(l, this.getVertexsLatLng()[1]);

        if  (dis1 < dis2) return this.getVertexsLatLng()[0];
        else              return this.getVertexsLatLng()[1];
    }

	public static double getDistance(Edge e, Landmark l){
		double min = 9999;
		for (PointD eTmp : e.getVertexsPoint()){
			double dis = (eTmp.x - l.getX()) * (eTmp.x - l.getX()) + (eTmp.y - l.getY()) * (eTmp.y - l.getY());
			if  (dis < min) min = dis;
		}
		return min;
	}

	public static double getDistance(PointD p1, PointD p2){
		return (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y);
	}

	public double getDistance(){
		return distance;
	}

	public boolean isVisited(){
		return visited;
	}

	public void setDistanceToEnd(double val){
		distanceToEnd = val;
	}

	public double getDistanceToEnd(){
		return distanceToEnd;
	}

	public List<Edge> getLinkedEdges(){
		return linkedEdges;
	}

	public void visit(){
		visited = true;
	}

	public void setEdgeToEnd(Edge e){
		toEnd = e;
	}

	public Edge getEdgeToEnd(){
		return toEnd;
	}

	public void searchInit(){
		visited = false;
		distanceToEnd = 9999;
		toEnd = null;
		v1Select = false;
		v2Select = false;
	}

	public static int comparePoint(PointD s, PointD d1, PointD d2)
	{
		double dis1 = (d1.x - s.x)*(d1.x - s.x) + (d1.y - s.y)*(d1.y - s.y);
		double dis2 = (d2.x - s.x)*(d2.x - s.x) + (d2.y - s.y)*(d2.y - s.y);
		if  (dis1 < dis2) return V1;
		else return V2;
	}

	public void selectVertex(int num){
		if  (num == V1) v1Select = true;
		else if  (num == V2) v2Select = true;
		else {
			System.out.println("vSelect error.");
			System.exit(0);
		}
	}

	public PointD getUnSelectPoint(){
		if  (v1Select) return v2;
		else if  (v2Select) return v1;
		else {
			System.out.println("vSelect error.");
			System.exit(0);
			return null;
		}
	}

	public static boolean isCrossing(PointD p, Edge e){
		Edge ep = new Edge(p.x, p.y, END_POINT_X, END_POINT_Y, -1, "ForCrossTest");
		return isCrossing(ep, e);
	}

	public static boolean isCrossing(Edge e1, Edge e2){
		double ta, tb, tc, td;
		PointD a = e1.getVertexsPoint()[0];
		PointD b = e1.getVertexsPoint()[1];
		PointD c = e2.getVertexsPoint()[0];
		PointD d = e2.getVertexsPoint()[1];

		tc = (a.x - b.x) * (c.y - a.y) + (a.y - b.y) * (a.x - c.x);
		td = (a.x - b.x) * (d.y - a.y) + (a.y - b.y) * (a.x - d.x);
		ta = (c.x - d.x) * (a.y - c.y) + (c.y - d.y) * (c.x - a.x);
		tb = (c.x - d.x) * (b.y - c.y) + (c.y - d.y) * (c.x - b.x);

		if  (tc * td < 0 && ta * tb < 0) return true;
		return false;
	}

	public PointD getMinimumPoint(PointD p){
		//getThetaByInnnerProduct.
		double edgeLen = distanceForCal;
		double epLen = (double)Math.sqrt((p.x - v1.x) * (p.x - v1.x) + (p.y - v1.y) * (p.y - v1.y));
		double innerProduct = (p.x - v1.x) * (v2.x - v1.x) + (p.y - v1.y) * (v2.y - v1.y);
		double rad = (double)Math.acos(innerProduct/(edgeLen * epLen));
		double theta = (double)Math.toDegrees(rad);
		//CaseByCase
		if (theta >= 90.0) return v1;
		else{
			double epCos = epLen * (double)Math.cos(rad);
			if  (epCos > edgeLen) return v2;
			else {
				double rate = epCos / edgeLen;
				return new PointD(v1.x + rate*(v2.x - v1.x), v1.y + rate*(v2.y - v1.y));
			}
		}
	}

	public Edge[] separate(PointD p){
		Edge[] res = new Edge[2];
		res[0] = new Edge(v1.x, v1.y,  p.x,  p.y, id + SEPARATED_EDGE_OFFSET    , name);
		res[1] = new Edge( p.x,  p.y, v2.x, v2.y, id + SEPARATED_EDGE_OFFSET + 1, name);
		return res;
	}

	public static void main(String[] args)
	{
		Edge e1 = new Edge( 1.0, 1.0, 3.0, 3.0, 111, "test");
		PointD p = e1.getMinimumPoint(new PointD(3, 1));
        System.out.println(p.x + " " + p.y);
	}
}
