package kei.balloon.pulldog;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Route
{
	private	List<Vertex> vertex; //ルートを構成する頂点リスト
    private List<Edge> edge;
	private List<Information> information; //ルートに関係のある情報リスト

	private Landmark start, end; //出発地と到着地のランドマークオブジェクト
	private double totalDistance; //総距離[m]
	private int[] direction;

    private MainActivity context;

	public final static int NAN       = -1;
	public final static int BOT       =  1;
	public final static int BOT_RIGHT =  2;
	public final static int RIGHT     =  3;
	public final static int TOP_RIGHT =  4;
	public final static int TOP       =  5;
	public final static int TOP_LEFT  =  6;
	public final static int LEFT      =  7;
	public final static int BOT_LEFT  =  8;

    private final static String FILENAME = "ExperimentForKitano/TagListDir.csv";
    private final static int FMAX = 1000;
	
	public Route(){

	}


	public Route(List<Vertex> path, Landmark s, Landmark e, double d, List<Edge> eg){
        vertex = path;
        start = s;
        end = e;
        totalDistance = d;
        information = new ArrayList<>();
        edge = eg;
        direction = new int[path.size()-2];
        setRouteDirectionsForHealthy();
	}

    public Route(List<Vertex> path, Landmark s, Landmark e, double d, List<Information> info, List<Edge> eg){
        vertex = path;
        start = s;
        end = e;
        totalDistance = d;
        information = info;
        edge = eg;
        direction = new int[path.size()-2];
        setRouteDirections();
    }

    public Route(MainActivity ma, List<Vertex> path, Landmark s, Landmark e, double d, List<Information> info, List<Edge> eg){
        context = ma;
        vertex = path;
        start = s;
        end = e;
        totalDistance = d;
        information = info;
        edge = eg;
        // todo baguru index...
        direction = new int[Math.max(path.size()-2, 1)];
        setRouteDirections();
    }

    public List<Information> getInformationById(int idNum){
        List<Information> res = new ArrayList<Information>();

        for (Information i : information){
            if  (i.getId() == idNum) res.add(i);
        }

        return res;
    }

    public List<LatLng> getPoints(){
        List<LatLng> res = new ArrayList<LatLng>();
        for (Vertex v : vertex){
            if  (!v.isInDoor()) res.add(new LatLng(v.getLat(), v.getLng()));
        }

        return res;
    }

    public List<Edge> getEdge(){
        return edge;
    }

    public List<Information> getInformation(){
        return information;
    }

	private void setRouteDirections(){
        String line;
        String[] tmp;
        int v1, v2, v3, dir;
        int[] d1, d2, d3, val;
        d1 = new int[FMAX];
        d2 = new int[FMAX];
        d3 = new int[FMAX];
        val = new int[FMAX];


        int itr = 0;

        try {
            //入力ストリームの作成
            InputStream fis = context.getAssets().open(FILENAME);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null){
                tmp = line.split(",");
                v1 = Integer.parseInt(tmp[0]);
                v2 = Integer.parseInt(tmp[1]);
                v3 = Integer.parseInt(tmp[2]);
                dir = Integer.parseInt(tmp[3]);
                d1[itr] = v1;
                d2[itr] = v2;
                d3[itr] = v3;
                val[itr] = dir;
                itr++;
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }


		for (int i=0; i<direction.length; i++){
			if  (!vertex.get(i).isInDoor() && !vertex.get(i+1).isInDoor() && !vertex.get(i+2).isInDoor()){
				direction[i] = getDirection(vertex.get(i), vertex.get(i+1), vertex.get(i+2));
			} else if (vertex.get(i).isInDoor() && vertex.get(i+1).isInDoor() && vertex.get(i+2).isInDoor()) {
				for (int j=0; j<itr; j++){
                    if  (d1[j]==vertex.get(i).getTagId() && d2[j]==vertex.get(i+1).getTagId() && d3[j]==vertex.get(i+2).getTagId()){
                        direction[i] = val[j];
                        break;
                    } else {
                        direction[i] = NAN;
                    }
                }
                Log.d("TAG", "vertex("+i+"):"+vertex.get(i).getTagId()+",vertex("+(i+1)+"):"+vertex.get(i+1).getTagId()+",vertex("+(i+2)+"):"+vertex.get(i+2).getTagId());
			} else {
                direction[i] = NAN;
            }

            Log.d("TAG", "direction["+i+"]:"+direction[i]);
		}
	}

    private void setRouteDirectionsForHealthy(){
        int pastdirection = -2;

        for (int i=0; i<direction.length; i++){
            int tmpd = getFourDirection(vertex.get(i), vertex.get(i + 1), vertex.get(i + 2));
            if  (!vertex.get(i).isInDoor() && !vertex.get(i+1).isInDoor() && !vertex.get(i+2).isInDoor() && tmpd != pastdirection){
                direction[i] = tmpd;
                pastdirection = tmpd;
                Log.d("TAG", "vertex("+i+"):"+vertex.get(i).getTagId()+",vertex("+(i+1)+"):"+vertex.get(i+1).getTagId()+",vertex("+(i+2)+"):"+vertex.get(i+2).getTagId());
            } else {
                direction[i] = NAN;
            }

            Log.d("TAG", "direction["+i+"]:"+direction[i]);
        }
    }

    public List<Vertex> getVertex(){
        return vertex;
    }

    public String getNextDirection(int index){
        if  (index >= direction.length || index < 0) return "";
        return numToMsg(direction[index]);
    }

    public Landmark getEndLandmark(){
        return end;
    }

    public Landmark getStartLandmark(){
        return start;
    }

	private int getDirection(Vertex first, Vertex mid, Vertex last)
	{
		PointD fp = first.getPoint();
		PointD mp =   mid.getPoint();
		PointD lp =  last.getPoint();

		double fmLen = (double)Math.sqrt(Edge.getDistance(fp, mp));
		double mlLen = (double)Math.sqrt(Edge.getDistance(mp, lp));

		double innerProduct = (lp.x - mp.x) * (fp.x - mp.x) + (lp.y - mp.y) * (fp.y - mp.y);

		double rad = (double)Math.acos(innerProduct/(fmLen * mlLen));
		int theta = (int)Math.toDegrees(rad);
		boolean plus = (fp.x - mp.x) * (lp.y - mp.y) - (fp.y - mp.y) * (lp.x - mp.x) >= 0 ? true : false; //外積 mp-lpベクトルの象限判定

		if  (!plus) theta = 360 - theta; 

		int dir = -1;
		if      (337.5 < theta || theta <=  22.5) dir = BOT;
		else if ( 22.5 < theta && theta <=  67.5) dir = BOT_RIGHT;
		else if ( 67.5 < theta && theta <= 112.5) dir = RIGHT;
		else if (112.5 < theta && theta <= 165.0) dir = TOP_RIGHT;
		else if (165.0 < theta && theta <= 195.0) dir = TOP;
		else if (195.0 < theta && theta <= 247.5) dir = TOP_LEFT;
		else if ( 22.5 < theta && theta <= 292.5) dir = LEFT;
		else if ( 22.5 < theta && theta <= 337.5) dir = BOT_LEFT;
		return dir;
	}

    private int getFourDirection(Vertex first, Vertex mid, Vertex last){
        PointD fp = first.getPoint();
        PointD mp =   mid.getPoint();
        PointD lp =  last.getPoint();

        double fmLen = (double)Math.sqrt(Edge.getDistance(fp, mp));
        double mlLen = (double)Math.sqrt(Edge.getDistance(mp, lp));

        double innerProduct = (lp.x - mp.x) * (fp.x - mp.x) + (lp.y - mp.y) * (fp.y - mp.y);

        double rad = (double)Math.acos(innerProduct/(fmLen * mlLen));
        int theta = (int)Math.toDegrees(rad);
        boolean plus = (fp.x - mp.x) * (lp.y - mp.y) - (fp.y - mp.y) * (lp.x - mp.x) >= 0 ? true : false; //外積 mp-lpベクトルの象限判定

        if  (!plus) theta = 360 - theta;

        int dir = -1;
        if      (315 < theta || theta <=  45) dir = BOT;
        else if ( 45 < theta && theta <= 135) dir = RIGHT;
        else if (135 < theta && theta <= 225) dir = TOP;
        else if (225 < theta && theta <= 315) dir = LEFT;
        return dir;
    }

	public void addInformation(Information info){
		information.add(info);
	}

	public void printInfo(){
		System.out.println("Start : " + start.getName());
		for (Vertex v : vertex){
			System.out.println(v.getId());
		}
		System.out.println("End   : " + end.getName());
	}

	public void printInfo(List<Edge> edges){
		System.out.println("Start : " + start.getName());
		for (Vertex v : vertex){
			if  (v.isInDoor()) System.out.println(v.getId());
			else {
				double minDistance = 9999999;
				Edge nearestEdge = null;
				PointD currentLocation = new PointD(v.getX(), v.getY());
				if  (vertex.indexOf(v) < vertex.size()-1){
					Vertex vNext = vertex.get(vertex.indexOf(v) + 1);
					if  (!vNext.isInDoor()){
						PointD currentLocation2 = new PointD(vNext.getX(), vNext.getY());
						for (Edge e : edges){
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
					for (Edge e : edges){
						PointD p = e.getMinimumPoint(currentLocation);
						double distance = Edge.getDistance(p, currentLocation); //現在地から対象点字ブロックへの最小距離
						if  (distance < minDistance){
							minDistance = distance;
							nearestEdge = e;
						}
					}
				}
				if  (nearestEdge != null) System.out.println(nearestEdge.getName());
			}
		}
		System.out.println("End   : " + end.getName());
	}

    private String numToMsg(int num){
        String msg;
        switch(num){
            case NAN:
                msg = "";
                break;
            case BOT:
                //msg = "後ろです";
                msg = "";
                break;
            case BOT_RIGHT:
                msg = "右斜め手前です";
                break;
            case RIGHT:
                msg = "右です";
                break;
            case TOP_RIGHT:
                msg = "右斜め前です";
                break;
            case TOP:
                msg = "前です";
                break;
            case TOP_LEFT:
                msg = "左斜め前です";
                break;
            case LEFT:
                msg = "左です";
                break;
            case BOT_LEFT:
                msg = "左斜め手前です";
                break;
            default:
                msg = "Error.";
                break;
        }
        return msg;
    }

	public void printDirections(){
		for (int d : direction){
			String msg = numToMsg(d);
			System.out.println(msg);
		}
	}


    public double getDistanceTo(LatLng current){
        double distance = 0.0;
        Vertex target = null;

        Edge currentEdge = GraphProcesser.getNearestEdge(edge, current);
        LatLng l1 = currentEdge.getVertexsLatLng()[0];
        LatLng l2 = currentEdge.getVertexsLatLng()[1];

        Vertex ev1 = GraphProcesser.getNearestVertex(vertex, l1);
        Vertex ev2 = GraphProcesser.getNearestVertex(vertex, l2);

        //どちらが先かを探索している。進行方向のVertexを取得するなら不等号は逆
        if  (vertex.indexOf(ev1) < vertex.indexOf(ev2)) target = ev1;
        if  (vertex.indexOf(ev2) < vertex.indexOf(ev1)) target = ev2;

        double tmpDis = GraphProcesser.toMeter(target.getLatLng(), current);

        if  (vertex.get(0).getId() == target.getId()) return tmpDis;

        for (int i=0; i<vertex.size()-1; i++){
            Vertex v1 = vertex.get(i);
            Vertex v2 = vertex.get(i + 1);
            if  (!v1.isInDoor() && !v2.isInDoor()){
              distance += GraphProcesser.toMeter(v1.getLatLng(), v2.getLatLng());
            } else if (v1.isInDoor() && v2.isInDoor()) {
                distance += Math.abs(v1.getDistanceToEnd() - v2.getDistanceToEnd());
            }

            if  (v2.getId() == target.getId()) break;
        }

        return distance + tmpDis;
    }

    public double getDistanceTo(int currentId){
        double distance = 0.0;

        for (int i=0; i<vertex.size()-1; i++){
            Vertex v1 = vertex.get(i);
            Vertex v2 = vertex.get(i + 1);
            if  (!v1.isInDoor() && !v2.isInDoor()){
                distance += GraphProcesser.toMeter(v1.getLatLng(), v2.getLatLng());
            } else if (v1.isInDoor() && v2.isInDoor()) {
                distance += Math.abs(v1.getDistanceToEnd() - v2.getDistanceToEnd());
            }

            if  (v2.getTagId() == currentId) break;
        }

        return distance;
    }

	public int getNextDirection(Vertex v){
		int vIndex = vertex.indexOf(v);
		if  (vIndex >= direction.length) return NAN;

		return direction[vIndex];
	}

    public double getTotalDistance(){
        return totalDistance;
    }

    public File outputForKml(MainActivity ma){
        KmlDecoder kd = new KmlDecoder();
        List<PointD> pList = new ArrayList<PointD>();
        for (Vertex v : vertex) pList.add(v.getPoint());

        return kd.toKml(pList, start, end, ma);
    }

	public static void main(String[] args){
		/*
			Vertex v1 = new Vertex(0, 0, new PointD(0, 0), false);
			Vertex v2 = new Vertex(0, 0, new PointD(0, 1), false);
			Vertex v3 = new Vertex(0, 0, new PointD(2, 0), false);
			Route testR = new Route();
			System.out.println(testR.getDirection(v1, v2, v3));
		*/
	}
}

