package kei.balloon.pulldog;




import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Area
{
	private List<Edge> edgeList;
	private PointD[] pointList;
	private int id;
	private int edgeRegistId = 10000;

	public Area(PointD[] pointData, String str){
		edgeList = new ArrayList<Edge>();
		pointList = pointData;	
		for (int i=0; i<pointData.length-1; i++){
			edgeList.add(new Edge(pointData[i].x, pointData[i].y, pointData[i+1].x,
									pointData[i+1].y, edgeRegistId++, "SafeArea" + edgeRegistId));
		}
	}

	public Area(List<PointD> pointData, String str){
		this((PointD[])pointData.toArray(new PointD[0]), str);
	}

	public void printAllPoints(){
		for (PointD p : pointList) System.out.print("(" + p.x + ", " + p.y + ") -- ");
		System.out.println("");
	}

	public void printAllEdges(){
		for (Edge e : edgeList){
			for (PointD p : e.getVertexsPoint()){
				System.out.print("(" + p.x + ", " + p.y + ")");
			}
			System.out.print(" : ");
		}
		System.out.println("");
	}

    public List<LatLng> getVertex(){
        List<LatLng> res = new ArrayList<LatLng>();
        for (PointD p : pointList) res.add(new LatLng(p.y, p.x));

        return  res;
    }

    public boolean includes(LatLng l){
        return this.includes(new PointD(l.longitude, l.latitude));
    }

	public boolean includes(PointD p){
		int crossCount = 0;
		for (Edge e : edgeList){
			if  (Edge.isCrossing(p, e)) crossCount++;
		}

		if  (crossCount % 2 == 1) return true;
		else return false;
	}

	public boolean includesCompletely(Edge e1){
		for (Edge e2 : edgeList){
			if  (Edge.isCrossing(e1, e2)) return false;
		}
		for (PointD p : e1.getVertexsPoint()){
			if  (!this.includes(p)) return false;
		}

		return true;
	}

	public static void main(String[] args){
		List<PointD> pList = new ArrayList<PointD>();
		pList.add(new PointD(1, 1));
		pList.add(new PointD(3, 1));
		pList.add(new PointD(3, 4));
		pList.add(new PointD(2, 4));
		pList.add(new PointD(2, 2));
		pList.add(new PointD(1, 2));
		pList.add(new PointD(1, 1));

		Area test = new Area(pList, "testArea");

		//test.printAllPoints();
		//test.printAllEdges();
		if  (test.includes(new PointD(1.5f, 1.5f))) System.out.println("Includes.");
		else System.out.println("Not includes.");
	}

}
