package kei.balloon.pulldog;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;


public class KmlDecoder
{
	private final static String MARK_STYLE = "ExperimentForKitano/MarkerStyle.kml";

	public KmlDecoder(){

	}

	public void decode(String inputFileName, String outputFileName){
		// ファイルオブジェクトの生成
	    File inputFile = new File(inputFileName);
	    File outputFile = new File(outputFileName);
	    try {
		    // 入力ストリームの生成
		    FileInputStream fis = new FileInputStream(inputFile);
		    InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		    BufferedReader br = new BufferedReader(isr);
	      	// 出力ストリームの生成
	      	FileOutputStream fos = new FileOutputStream(outputFile);
	   	 	OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
	  	    PrintWriter pw = new PrintWriter(osw);


		    String data;
		    while(true){
			    while(true){
			    	data = br.readLine();
			    	if  (data == null) break;
			    	if  (data.indexOf("<coordinates>") != -1) break;
				}
				if  (data == null) break;
				data = br.readLine();
				if  (data.indexOf("coordinates") == -1) pw.println(data.replaceAll("\t", "").replaceAll(",0", ""));
			}
		    br.close();
		    pw.close();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	}

		public void decodeForGoogle(String inputFileName, String outputFileName){
		// ファイルオブジェクトの生成
	    File inputFile = new File(inputFileName);
	    File outputFile = new File(outputFileName);
	    try {
		    // 入力ストリームの生成
		    FileInputStream fis = new FileInputStream(inputFile);
		    InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		    BufferedReader br = new BufferedReader(isr);
	      	// 出力ストリームの生成
	      	FileOutputStream fos = new FileOutputStream(outputFile);
	   	 	OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
	  	    PrintWriter pw = new PrintWriter(osw);


		    String data, name = null;
		    while(true){
			    while(true){
			    	data = br.readLine();
			    	if  (data == null) break;
			    	if  (data.indexOf("<name>")        != -1) break;
			    	if  (data.indexOf("<coordinates>") != -1) break;
				}
				if  (data == null) break;
				if  (data.indexOf("<name>") != -1) name = data.substring(9, data.length()-7);
				else pw.println(data.substring(13+4, data.length()-14).replaceAll("\t", "").replaceAll(",0.0", "") + " " + name);
			}
		    br.close();
		    pw.close();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	}

	public void decodeSafeArea(String inputFileName, String outputFileName){
		// ファイルオブジェクトの生成
	    File inputFile = new File(inputFileName);
	    File outputFile = new File(outputFileName);
	    try {
		    // 入力ストリームの生成
		    FileInputStream fis = new FileInputStream(inputFile);
		    InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		    BufferedReader br = new BufferedReader(isr);
	      	// 出力ストリームの生成
	      	FileOutputStream fos = new FileOutputStream(outputFile);
	   	 	OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
	  	    PrintWriter pw = new PrintWriter(osw);
	  	    pw.println("SafeArea");

		    String data, name = null;
		    while(true){
			    while(true){
			    	data = br.readLine();
			    	if  (data == null) break;
			    	if  (data.indexOf("<name>")        != -1) break;
			    	if  (data.indexOf("<coordinates>") != -1) break;
				}
				if  (data == null) break;
				if  (data.indexOf("<name>") != -1) name = data.substring(9, data.length()-7);
				else pw.println(data.substring(13+6, data.length()-14).replaceAll("\t", "").replaceAll(",0.0", "") + " " + name);
			}
		    br.close();
		    pw.close();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	}


	public void toKitanoStation(BigDecimal top, BigDecimal right, BigDecimal bottom, BigDecimal left, String inputFileName, String outputFileName){
		String[] tmpData1;
		String[] tmpData2;
		String line;
		BigDecimal x,y;
		// ファイルオブジェクトの生成
	    File inputFile = new File(inputFileName);
	    File outputFile = new File(outputFileName);
	    try {
		    // 入力ストリームの生成
		    FileInputStream fis = new FileInputStream(inputFile);
		    InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		    BufferedReader br = new BufferedReader(isr);
	      	// 出力ストリームの生成
	      	FileOutputStream fos = new FileOutputStream(outputFile);
	   	 	OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
	  	    PrintWriter pw = new PrintWriter(osw);

	  	    while ((line = br.readLine()) != null){
				boolean include = true;
				String out = new String();

	  	    	tmpData1 = line.split(" ");
	  	    	for (String s : tmpData1){
	  	    		tmpData2 = s.split(",");
	  	    		x = new BigDecimal(tmpData2[0]);
	  	    		y = new BigDecimal(tmpData2[1]);
	  	    		if  (x.compareTo(left) < 0 || x.compareTo(right) > 0 || y.compareTo(bottom) < 0 || y.compareTo(top) > 0){
	  	    			include = false;
	  	    			break;
	  	    		}
	  	    		out += x + "," + y + ",0 ";
	  	    	}
	  	    	if  (include){
	  	    		out = out.substring(0, out.length()-1);
	  	    		pw.println(out);
	  	    	}
	  	    }

		    br.close();
		    pw.close();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	}


	void toKml(String inputFileName, String outputFileName){
		String line;
		String[] tmpData1;
		double[] tmpData2;
		// ファイルオブジェクトの生成
	    File inputFile = new File(inputFileName);
	    File outputFile = new File(outputFileName);
	    try {
		    // 入力ストリームの生成
		    FileInputStream fis = new FileInputStream(inputFile);
		    InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
		    BufferedReader br = new BufferedReader(isr);
	      	// 出力ストリームの生成
	      	FileOutputStream fos = new FileOutputStream(outputFile);
	   	 	OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
	  	    PrintWriter pw = new PrintWriter(osw);

	  	    pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	  	    pw.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
	  	    pw.println("<Document>");
			pw.println("\t<name>誘導ブロック（線状）.kml</name>");
			pw.println("\t<StyleMap id=\"m_ylw-pushpin1\">");
			pw.println("\t\t<Pair>");
			pw.println("\t\t\t<key>normal</key>");
			pw.println("\t\t\t<styleUrl>#s_ylw-pushpin</styleUrl>");
			pw.println("\t\t</Pair>");
			pw.println("\t\t<Pair>");
			pw.println("\t\t\t<key>highlight</key>");
			pw.println("\t\t\t<styleUrl>#s_ylw-pushpin_hl00</styleUrl>");
			pw.println("\t\t</Pair>");
			pw.println("\t</StyleMap>");
			pw.println("\t<Style id=\"s_ylw-pushpin\">");
			pw.println("\t\t<IconStyle>");
			pw.println("\t\t\t<scale>1.1</scale>");
			pw.println("\t\t\t<Icon>");
			pw.println("\t\t\t\t<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>");
			pw.println("\t\t\t</Icon>");
			pw.println("\t\t\t<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>");
			pw.println("\t\t</IconStyle>");
			pw.println("\t\t<LineStyle>");
			pw.println("\t\t\t<color>ff00ffff</color>");
			pw.println("\t\t\t<width>3</width>");
			pw.println("\t\t</LineStyle>");
			pw.println("\t</Style>");
			pw.println("\t<Style id=\"s_ylw-pushpin_hl00\">");
			pw.println("\t\t<IconStyle>");
			pw.println("\t\t\t<scale>1.3</scale>");
			pw.println("\t\t\t<Icon>");
			pw.println("\t\t\t\t<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>");
			pw.println("\t\t\t</Icon>");
			pw.println("\t\t\t<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>");
			pw.println("\t\t</IconStyle>");
			pw.println("\t\t<LineStyle>");
			pw.println("\t\t\t<color>ff00ffff</color>");
			pw.println("\t\t\t<width>3</width>");
			pw.println("\t\t</LineStyle>");
			pw.println("\t</Style>");
			pw.println("\t<Folder>");
			pw.println("\t\t<name>" + outputFileName.substring(0, outputFileName.length()-4) + "</name>");

			while ((line = br.readLine()) != null){
				pw.println("\t\t<Placemark>");
				pw.println("\t\t\t<name>無題 - パス</name>");
				pw.println("\t\t\t<styleUrl>#m_ylw-pushpin1</styleUrl>");
				pw.println("\t\t\t<LineString>");
				pw.println("\t\t\t\t<tessellate>1</tessellate>");
				pw.println("\t\t\t\t<coordinates>");

				pw.println("\t\t\t\t\t" + line);

				pw.println("\t\t\t\t</coordinates>");
				pw.println("\t\t\t</LineString>");
				pw.println("\t\t</Placemark>");
			}

			pw.println("\t</Folder>");
			pw.println("</Document>");
			pw.println("</kml>");

		    br.close();
		    pw.close();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	}

    public File toKml(List<PointD> route, Landmark start, Landmark end, MainActivity ma){

        String line;
        Context context = ma;
        AssetManager am = context.getAssets();
        context = ma.getApplicationContext();
        File inputFile = new File(MARK_STYLE);
        File outputFile = new File(context.getFilesDir(),"Route.kml");
        try{
            // 入力ストリームの生成
            InputStream fis = am.open(MARK_STYLE);
            InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
            BufferedReader markStyle = new BufferedReader(isr);

            // 出力ストリームの生成
            FileOutputStream fos = new FileOutputStream(outputFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
            PrintWriter pw = new PrintWriter(osw);

            pw.println("<?xml version='1.0' encoding='UTF-8'?>");
            pw.println("<kml xmlns='http://www.opengis.net/kml/2.2'>");
            pw.println("\t<Document>");
            pw.println("\t\t<name>Route</name>");

            //Route
            pw.println("\t\t<Placemark>");
            pw.println("\t\t\t<name>Route</name>");
            pw.println("\t\t\t<styleUrl>#line-FFFF00-3-nodesc</styleUrl>");
            pw.println("\t\t\t<LineString>");
            pw.println("\t\t\t\t<tessellate>1</tessellate>");
            pw.print("\t\t\t\t<coordinates>");
            for (PointD p : route){
                pw.print(p.x + "," + p.y);
                if  (route.indexOf(p) < route.size() - 1) pw.print(" ");
            }
            pw.println("</coordinates>");
            pw.println("\t\t\t</LineString>");
            pw.println("\t\t</Placemark>");

            //Marker(Start)
            pw.println("\t\t<Placemark>");
            pw.println("\t\t\t<name>" + start.getName() + "(Departure)</name>");
            pw.println("\t\t\t<styleUrl>#icon-503-41F08C-nodesc</styleUrl>");
            pw.println("\t\t\t<PointD>");
            pw.println("\t\t\t\t<tessellate>1</tessellate>");
            pw.print("\t\t\t\t<coordinates>" + start.getX() + "," + start.getY() + ",0");
            pw.println("</coordinates>");
            pw.println("\t\t\t</PointD>");
            pw.println("\t\t</Placemark>");

            //Marker(End)
            pw.println("\t\t<Placemark>");
            pw.println("\t\t\t<name>" + end.getName() + "(Destination)</name>");
            pw.println("\t\t\t<styleUrl>#icon-503-41F08C-nodesc</styleUrl>");
            pw.println("\t\t\t<PointD>");
            pw.println("\t\t\t\t<tessellate>1</tessellate>");
            pw.print("\t\t\t\t<coordinates>" + end.getX() + "," + end.getY() + ",0");
            pw.println("</coordinates>");
            pw.println("\t\t\t</PointD>");
            pw.println("\t\t</Placemark>");

            //DefineStyle
            pw.println("\t\t<Style id='line-FFFF00-3-nodesc-normal'>");
            pw.println("\t\t\t<LineStyle>");
            pw.println("\t\t\t\t<color>ffFF5555</color>");
            pw.println("\t\t\t\t<width>3</width>");
            pw.println("\t\t\t</LineStyle>");
            pw.println("\t\t\t<BalloonStyle>");
            pw.println("\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>");
            pw.println("\t\t\t</BalloonStyle>");
            pw.println("\t\t</Style>");
            pw.println("\t\t<Style id='line-FFFF00-3-nodesc-highlight'>");
            pw.println("\t\t\t<LineStyle>");
            pw.println("\t\t\t\t<color>ffFF5555</color>");
            pw.println("\t\t\t\t<width>5.0</width>");
            pw.println("\t\t\t</LineStyle>");
            pw.println("\t\t\t<BalloonStyle>");
            pw.println("\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>");
            pw.println("\t\t\t</BalloonStyle>");
            pw.println("\t\t</Style>");
            pw.println("\t\t<StyleMap id='line-FFFF00-3-nodesc'>");
            pw.println("\t\t\t<Pair>");
            pw.println("\t\t\t\t<key>normal</key>");
            pw.println("\t\t\t\t<styleUrl>#line-FFFF00-3-nodesc-normal</styleUrl>");
            pw.println("\t\t\t</Pair>");
            pw.println("\t\t\t<Pair>");
            pw.println("\t\t\t\t<key>highlight</key>");
            pw.println("\t\t\t\t<styleUrl>#line-FFFF00-3-nodesc-highlight</styleUrl>");
            pw.println("\t\t\t</Pair>");
            pw.println("\t\t</StyleMap>");

            //DefineMarkStyle
            while ((line = markStyle.readLine()) != null){
                pw.println(line);
            }

            pw.println("\t</Document>");
            pw.println("</kml>");

            markStyle.close();
            pw.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return outputFile;
    }

	public static void main(String[] args){
		BigDecimal top    = new BigDecimal(35.6452653);
		BigDecimal right  = new BigDecimal(139.3533003);
		BigDecimal bottom = new BigDecimal(35.6446506);
		BigDecimal left   = new BigDecimal(139.3520987);



		KmlDecoder test = new KmlDecoder();
		//test.decode("PointDBlock.kml", "ProcessedPointDBlock.txt");
		//test.decodeSafeArea("ExperimentForKitano/SafeArea.kml", "ExperimentForKitano/SafeArea.txt");
		//test.decodeForGoogle("ExperimentForKitano/KitanoLine.kml", "ExperimentForKitano/KitanoLine.txt");
		test.decodeForGoogle("ExperimentForKitano/ランドマーク.kml", "ExperimentForKitano/ランドマーク.txt");
		//test.decodeForGoogle("ExperimentForKitano/KitanoPointD.kml", "ExperimentForKitano/KitanoPointD.txt");
		//test.toKitanoStation(top, right, bottom, left, "ProcessedPointDBlock.txt", "KitanoLineBlock.txt");
		//test.toKml("KitanoLineBlock.txt", "KitanoLine.kml");
	}
}
