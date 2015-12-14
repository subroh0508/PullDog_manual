package kei.balloon.pulldog;

import android.text.format.Time;

import com.google.android.gms.maps.model.LatLng;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by root on 15/09/16.
 */
public class RecordingCSV {
	private OutputStreamWriter sw;
	private PrintWriter pw;
	private String fileName;

	private List<LatLng> route = new ArrayList<>();
	private List<String> time = new ArrayList<>();
	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss yyyy/MM/dd");

	public RecordingCSV(String s) {
		fileName = s;
		try {
			sw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
			pw = new PrintWriter(sw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void recordData(LatLng point) {
		Date date = new Date(System.currentTimeMillis());
		time.add(dateFormat.format(date));

		route.add(point);
	}

	public boolean closeFile(){
		try{
			int i = 0;
			for(LatLng p : route) {
				pw.println(p.latitude + "," + p.longitude + ","+time.get(i));
				i++;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
