package kei.balloon.pulldog;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

/**
 * Created by subroh0508 on 15/12/03.
 */
public class MapFragment extends Fragment {
	private NowLocation nowLocation = null;
	private GoogleMap googleMap;
	private Handler mapHandler = null;
	public Marker nowMarker = null;
	private TextView latlngText;
	private boolean threadIsStopped = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.activity_main, container, false);
		setUpMapIfNeeded();

		latlngText = (TextView)rootView.findViewById(R.id.latlang);

		Bundle infoBundle = new Bundle();
		if(nowLocation != null) nowLocation = (NowLocation)infoBundle.getSerializable("NowLocation");
		if(mapHandler != null) mapHandler = new Handler();

		if(threadIsStopped) {
			new Thread(mapLoop).start();
			threadIsStopped = false;
		}

		return rootView;
	}

	private final Runnable mapLoop = new Runnable() {
		@Override
		public void run() {
			mapHandler.post(new Runnable() {
				@Override
				public void run() {
					if(nowLocation != null) {
						double lat = nowLocation.getPointLat();
						double lng = nowLocation.getPointLng();

						updateMap(lat, lng);
						latlngText.setText("("+lat+","+lng+")");
					}
				}
			});
		}
	};

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (googleMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			FragmentActivity activity = (FragmentActivity)getActivity();
			googleMap = ((SupportMapFragment) activity.getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
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
