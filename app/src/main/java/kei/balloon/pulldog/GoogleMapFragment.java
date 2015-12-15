package kei.balloon.pulldog;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by subroh0508 on 15/12/03.
 */
public class GoogleMapFragment extends Fragment {
	private final static short RFID_SURVEY = 0, GNSS_SURVEY = 1, BOTH_SURVEY = 2;
	private final static int RED = 0x66FF0000, ORANGE = 0x66FF6600, YELLOW = 0xFFFFFF00,
			GREEN = 0x6600FF00, BLUE = 0x660000FF, WHITE = 0x00FFFFFF, BLACK = 0xFF000000;

	private SupportMapFragment mapFragment;

	private NowLocation nowLocation = null;
	private GoogleMap googleMap;
	public Marker nowMarker = null;

	private Bundle mapBundle = null;
	private Handler mapHandler = null;
	private TextView qzssOn, rfidOn, tagNumber, latlngText;
	private EditText fileName;
	private Button recordSurvey;
	private boolean threadIsStopped = true;

	private RecordingKML kml = null, gnssCsv = null, rfidCsv = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.map_fragment, container, false);

		qzssOn = (TextView)rootView.findViewById(R.id.qzss_on);
		rfidOn = (TextView)rootView.findViewById(R.id.rfid_on);
		tagNumber = (TextView)rootView.findViewById(R.id.tag_number);
		latlngText = (TextView)rootView.findViewById(R.id.latlang);

		fileName = (EditText)rootView.findViewById(R.id.filename);

		recordSurvey = (Button)rootView.findViewById(R.id.record_survey);

		updateTextView();

		if(mapHandler == null) mapHandler = new Handler();

		if(mapBundle == null) mapBundle = getArguments();
		if(nowLocation == null)nowLocation = (NowLocation)mapBundle.getSerializable("NowLocation");

		recordSurvey.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (kml == null) {
					String name = fileName.getText().toString();
					String path = R.string.file_path + name + ".csv";
					kml = new RecordingKML(path);

					path = R.string.file_path + name + "_gnss.csv";
					gnssCsv = new RecordingKML(path);
					path = R.string.file_path + name + "_rfid.csv";
					rfidCsv = new RecordingKML(path);

					recordSurvey.setText("REC FINISH");
				} else {
					kml.closeFile();
					gnssCsv.closeFile();
					rfidCsv.closeFile();
					kml = null;
					gnssCsv = null;
					rfidCsv = null;

					recordSurvey.setText("REC START");
				}
			}
		});

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		FragmentManager fm = getChildFragmentManager();
		mapFragment = (SupportMapFragment)fm.findFragmentById(R.id.map_frag);
		if (mapFragment == null) {
			mapFragment = SupportMapFragment.newInstance();
			fm.beginTransaction().replace(R.id.map_frag, mapFragment).commit();
		}

		setUpMapIfNeeded();

		if(threadIsStopped) {
			new Thread(mapLoop).start();
			threadIsStopped = false;
		}
	}

	@Override
	public void onPause(){
		super.onPause();

		threadIsStopped = true;
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		ft.remove(mapFragment).commit();
		googleMap = null;
	}

	private final Runnable mapLoop = new Runnable() {
		@Override
		public void run() {
			while(!threadIsStopped) {
				if (nowLocation != null) {

					mapHandler.post(new Runnable() {
						@Override
						public void run() {
							updateTextView();

							tagNumber.setText(String.valueOf(nowLocation.getTagId()));
							LatLng surveyPoint = nowLocation.getNowPoint();

							updateMap(surveyPoint.latitude, surveyPoint.longitude);
							if(kml != null) kml.recordData(surveyPoint);
							if(gnssCsv != null) gnssCsv.recordData(nowLocation.getGnssPoint());
							if(rfidCsv != null) rfidCsv.recordData(nowLocation.getRfidPoint());

							latlngText.setText("(" + surveyPoint.latitude
									+ "," + surveyPoint.longitude + ")");
						}
					});

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.getStackTrace();
					}
					//Log.d("map", "Loop");
				}
			}
		}

	};

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (googleMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			googleMap = mapFragment.getMap();
			// Check if we were successful in obtaining the map.
			if (googleMap != null) {
				updateMap(0.0, 0.0);
			}
		} else {
			if(nowLocation != null) {
				LatLng surveyPoint = nowLocation.getNowPoint();
				updateMap(surveyPoint.latitude, surveyPoint.longitude);
			} else
				updateMap(0.0, 0.0);
		}
	}

	public void updateMap(double lat, double lng) {
		LatLng p = new LatLng(lat, lng);

		if (nowMarker != null) nowMarker.remove();
		nowMarker = googleMap.addMarker(new MarkerOptions().position(p).title("げんざいち"));

		//CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 25f);
		//googleMap.moveCamera(cu);
	}

	public void updateTextView() {
		if(nowLocation != null){
			short mode = nowLocation.surveyModeSelect();
			switch(mode){
				case RFID_SURVEY:
					qzssOn.setText("QZSS_OFF");
					rfidOn.setText("RFID ON");
					break;
				case BOTH_SURVEY:
					qzssOn.setText("QZSS_ON");
					rfidOn.setText("RFID_ON");
					break;
				case GNSS_SURVEY:
					qzssOn.setText("QZSS_ON");
					rfidOn.setText("RFID OFF");
					break;
			}

			if(nowLocation.SURVEY_EN) {
				qzssOn.setBackgroundColor(YELLOW);
				rfidOn.setBackgroundColor(YELLOW);

				if(nowLocation.CORRECTION_ON) {
					qzssOn.setBackgroundColor(GREEN);
					rfidOn.setBackgroundColor(GREEN);
				}
			} else {
				qzssOn.setBackgroundColor(RED);
				rfidOn.setBackgroundColor(RED);
			}
		} else {
			qzssOn.setBackgroundColor(RED);
			rfidOn.setBackgroundColor(RED);
		}
	}
}
