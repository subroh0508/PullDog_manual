package kei.balloon.pulldog;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

/**
 * Created by subroh0508 on 15/12/03.
 */
public class GoogleMapFragment extends Fragment {
	private SupportMapFragment mapFragment;

	private NowLocation nowLocation = null;
	private GoogleMap googleMap;
	public Marker nowMarker = null;

	private Bundle mapBundle = null;
	private Handler mapHandler = null;
	private TextView latlngText;
	private boolean threadIsStopped = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.activity_main, container, false);

		latlngText = (TextView)rootView.findViewById(R.id.latlang);

		if(mapHandler == null) mapHandler = new Handler();

		if(mapBundle == null) mapBundle = new Bundle();
		nowLocation = (NowLocation)mapBundle.getSerializable("NowLocation");

		if(threadIsStopped) {
			new Thread(mapLoop).start();
			threadIsStopped = false;
		}

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		FragmentManager fm = getChildFragmentManager();
		mapFragment = (SupportMapFragment)fm.findFragmentById(R.id.map_container);
		if (mapFragment == null) {
			mapFragment = SupportMapFragment.newInstance();
			fm.beginTransaction().replace(R.id.map_container, mapFragment).commit();
		}
		setUpMapIfNeeded();
	}

	@Override
	public void onPause(){
		super.onPause();


		threadIsStopped = true;
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		ft.remove(mapFragment).commit();
	}

	private final Runnable mapLoop = new Runnable() {
		@Override
		public void run() {
			while(!threadIsStopped) {
				nowLocation = (NowLocation)mapBundle.getSerializable("NowLocation");

				mapHandler.post(new Runnable() {
					@Override
					public void run() {
						if (nowLocation != null) {
							LatLng surveyPoint = nowLocation.getNowPoint();

							updateMap(surveyPoint.latitude, surveyPoint.longitude);
							latlngText.setText("(" + surveyPoint.latitude
									+ "," + surveyPoint.longitude + ")");
						}
					}
				});

				try {
					Thread.sleep(500);
				} catch(InterruptedException e) {
					e.getStackTrace();
				}
				//Log.d("map", "Loop");
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
		}
	}

	public void updateMap(double lat, double lng) {
		LatLng p = new LatLng(lat, lng);

		if (nowMarker != null) nowMarker.remove();
		nowMarker = googleMap.addMarker(new MarkerOptions().position(p).title("げんざいち"));

		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 25f);
		googleMap.moveCamera(cu);
	}
}
