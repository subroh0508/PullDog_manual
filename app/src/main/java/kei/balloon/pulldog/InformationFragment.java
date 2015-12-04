package kei.balloon.pulldog;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by subroh0508 on 15/12/03.
 */
public class InformationFragment extends Fragment{
	private Bundle infoBundle = null;
	private Qzss qzssInfo = null;
	private Handler infoHandler = null;

	private TextView sateliteCount;
	private TextView gnssLog;
	private boolean threadIsStopped = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.info_fragment, container, false);

		sateliteCount = (TextView)rootView.findViewById(R.id.satelite_count);
		gnssLog = (TextView)rootView.findViewById(R.id.gnss_log_view);

		if(infoHandler == null) infoHandler = new Handler();
		if(infoBundle == null) infoBundle = new Bundle();
		qzssInfo = (Qzss)infoBundle.getSerializable("QZSS");

		if(threadIsStopped){
			new Thread(infoLoop).start();
			threadIsStopped = false;
		}

		return rootView;
	}

	@Override
	public void onPause(){
		super.onPause();

		threadIsStopped = true;
	}

	private final Runnable infoLoop = new Runnable() {
		@Override
		public void run() {
			while (!threadIsStopped) {
				qzssInfo = (Qzss)infoBundle.getSerializable("QZSS");

				infoHandler.post(new Runnable() {
					@Override
					public void run() {
						int visible = 0;
						int useful = 0;

						if (qzssInfo != null) {
							visible = qzssInfo.getVisibleCount();
							useful = qzssInfo.getUsefulCount();

							gnssLog.append(qzssInfo.getLog() + "\n");
						}

						sateliteCount.setText("visible:" + visible + "  useful:" + useful);
					}
				});

				try {
					Thread.sleep(500);
				} catch(InterruptedException e) {
					e.getStackTrace();
				}

				//Log.d("info", "Loop");
			}

		}
	};
}
