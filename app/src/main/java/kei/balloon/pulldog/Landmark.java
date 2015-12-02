package kei.balloon.pulldog;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Landmark
{
	int id;
	int tagId;
	String name;
	PointD point;
	private List<Edge> linkedEdges;
	private boolean inDoor;
	private boolean linker;
	private Landmark linkedLandmark;

	private boolean checked;

	public final static int HERE_ID = 5555;
    public final static int RELAY_ID = 6666;


	public Landmark(double x, double y, int idNum, String str, boolean isLinker){
		point = new PointD(x, y);
		id = idNum;
		tagId = -1;
		name = str;
		inDoor = false;
		linker = isLinker;
		linkedEdges = new ArrayList<Edge>();
		linkedLandmark = null;
	}

	public Landmark(int tagIdNum, int idNum, String str, boolean isLinker){
		point = null;
		id = idNum;
		tagId = tagIdNum;
		name = str;
		inDoor = true;
		linker = isLinker;
		linkedEdges = new ArrayList<Edge>();
		linkedLandmark = null;
	}

	public void setLinkedLandmark(Landmark l){
		linkedLandmark = l;
	}

	public Landmark getLinkedLandmark(){
		return linkedLandmark;
	}

	public void addLinkedObject(Edge e){
		linkedEdges.add(e);
	}

    public void clearLinkedObject(){
        linkedEdges.clear();
    }

	public List<Edge> getLinkedEdges(){
		return linkedEdges;
	}

	public double getX(){
		return point.x;
	}

	public double getY(){
		return point.y;
	}

    public double getLng(){
        return point.x;
    }

    public double getLat(){
        return point.y;
    }

    public LatLng getLatLng(){
        LatLng l = new LatLng(point.y,point.x);
        return l;
    }


    public boolean isInDoor(){
		return inDoor;
	}

	public boolean isLinker(){
		return linker;
	}

	public PointD getPoint(){
		return point;
	}

	public String getName(){
		return name;
	}

	public int getId(){
		return id;
	}

	public int getTagId(){
		return tagId;
	}

	public void searchInit(){
		checked = false;
	}

	public void check(){
		checked = true;
	}

	public boolean isChecked(){
		return checked;
	}

	public void printLinkedObjects(){
		System.out.println("<" + name + ">");
		for (Edge e : linkedEdges) System.out.print(e.getName() + " ");
		System.out.println("");
		System.out.println("");
	}

	public static void main(String[] args){

	}
}
