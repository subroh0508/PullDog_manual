package kei.balloon.pulldog;

//ただのデータ構造．接続されている頂点とそこへの距離を一意に対応付けるために必要かな？
public class Distance
{
	public Vertex linkedVertex;
	public double distance;

	public Distance(Vertex v, double d){
		linkedVertex = v;
		distance = d;
	}
}
