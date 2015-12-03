package kei.balloon.pulldog;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by subroh0508 on 15/12/03.
 */
public class InformationFragment extends Fragment{
	private QZSS qzssInfo = null;
	private Handler infoHandler = null;
	private TextView sateliteCount;
	private boolean threadIsStopped = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.info_fragment, container, false);

		sateliteCount = (TextView)rootView.findViewById(R.id.satelite_count);

		Bundle infoBundle = new Bundle();
		if(qzssInfo != null) qzssInfo = (QZSS)infoBundle.getSerializable("QZSS");
		if(infoHandler != null) infoHandler = new Handler();

		if(threadIsStopped){
			new Thread(infoLoop).start();
			threadIsStopped = false;
		}

		return rootView;
	}

	private final Runnable infoLoop = new Runnable() {
		@Override
		public void run() {
			infoHandler.post(new Runnable() {
				@Override
				public void run() {
					sateliteCount.setText("visible:" + qzssInfo.getVisibleCount()
							+ "  useful:" + qzssInfo.getUsefulCount());
				}
			});

		}
	};
}
