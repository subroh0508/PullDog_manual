package kei.balloon.pulldog;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Vertex
{
	private boolean inDoor; //屋内:true  屋外:false
	private PointD point; //座標(任意)
	private int tagId; //RFIDタグID．屋内ならば必須,屋外なら任意．
	private int uniqueId; //ユニークID
	private List<Distance> linkedVertex; //接続されているVertex．屋外ならば計算から，屋内ならばデータベースから取得
	private String name;
	public final static boolean IN  = true;
	public final static boolean OUT = false;

	private boolean visited;
	private boolean visitedForNavi;
	private Vertex toEnd;
	private double distanceToEnd;



	public Vertex(int idNum, int idNum2, String str, PointD pt, boolean isInDoor){
		uniqueId = idNum;
		tagId = idNum2;
		name = str;
		linkedVertex = new ArrayList<Distance>();
		point = pt;
		inDoor = isInDoor;
	}

	//for Out
	public Vertex(int idNum, PointD pt, boolean isInDoor){
		this(idNum, -1, null, pt, isInDoor);
	}

	//for Out2
	public Vertex(int idNum, String str, PointD pt, boolean isInDoor){
		this(idNum, -1, str, pt, isInDoor);
	}

    //for Out3
    public Vertex(int idNum, int idNum2, PointD pt, boolean isInDoor){
        this(idNum, idNum2, null, pt, isInDoor);
    }

	//for In
	public Vertex(int idNum, int idNum2, boolean isInDoor){
		this(idNum, idNum2, null, isInDoor);
	}

	public int getId(){ return uniqueId; }

	public int getTagId(){
		return tagId;
	}

	public boolean isInDoor(){
		return inDoor;
	}

	public boolean isVisited(){
		return visited;
	}

	public boolean isVisitedForNavi() { return visitedForNavi; }

	public void visit(){
		visited = true;
	}

	public void visitForNavi() { visitedForNavi = true; }

	public void searchInit(){
		visited = false;
		toEnd = null;
		distanceToEnd = 9999999;
	}

	public void addLinkedVertex(Vertex v, double d){
		linkedVertex.add(new Distance(v, d));
	}

	public List<Distance> getLinkedVertexs(){
		return linkedVertex;
	}

	public double getDistanceToEnd(){
		return distanceToEnd;
	}

	public void setVertexToEnd(Vertex v){
		toEnd = v;
	}

	public Vertex getVertexToEnd(){
		return toEnd;
	}

	public double getX(){
		return point.x;
	}

	public double getY(){
		return point.y;
	}

    public double getLat(){
        return point.y;
    }

    public double getLng(){
        return point.x;
    }

    public LatLng getLatLng(){
        return new LatLng(getLat(), getLng());
    }

	public PointD getPoint(){
		return point;
	}

	public void setDistanceToEnd(double d){ distanceToEnd = d;
	}

	public void printInformation(){
		System.out.println("[UID:" + uniqueId + ", TagID:" + tagId + "]");
		System.out.print("		[LinkedVertex] : ");
		for (Distance d : linkedVertex){
			System.out.print(d.linkedVertex.getId() + "(d:" + d.distance + ") ");
		}
		System.out.println("\n");
	}

	public static void main(String[] args){
		System.out.println("Class Vertex Main");
	}
}
