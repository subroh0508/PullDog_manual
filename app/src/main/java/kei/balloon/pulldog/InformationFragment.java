package kei.balloon.pulldog;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by subroh0508 on 15/12/03.
 */
public class InformationFragment extends Fragment{
	private Bundle infoBundle = null;
	private Qzss qzssInfo = null;
	private Handler infoHandler = null;
	private MainActivity activity;

	private TextView sateliteCount, gnssLog;
	private EditText fileName;
	private Button convertCsv;
	private boolean threadIsStopped = true;

	private ConvertCsvToKml csvToKml;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.info_fragment, container, false);

		sateliteCount = (TextView)rootView.findViewById(R.id.satelite_count);
		gnssLog = (TextView)rootView.findViewById(R.id.gnss_log_view);

		fileName = (EditText)rootView.findViewById(R.id.filename_csv);
		convertCsv = (Button)rootView.findViewById(R.id.convert_csv_to_kml);

		if(infoHandler == null) infoHandler = new Handler();
		if(infoBundle == null) infoBundle = getArguments();
		if(qzssInfo == null) qzssInfo = (Qzss)infoBundle.getSerializable("QZSS");

		if(threadIsStopped){
			new Thread(infoLoop).start();
			threadIsStopped = false;
		}

		convertCsv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				csvToKml = new ConvertCsvToKml(fileName.getText().toString());
				csvToKml.convert();
			}
		});

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
			while(!threadIsStopped) {
				if (qzssInfo != null) {
					infoHandler.post(new Runnable() {
						@Override
						public void run() {
							int visible = 0;
							int useful = 0;

							visible = qzssInfo.getVisibleCount();
							useful = qzssInfo.getUsefulCount();

							gnssLog.append(qzssInfo.getLog() + "\n");

							sateliteCount.setText("visible:" + visible + "  useful:" + useful);
						}
					});

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.getStackTrace();
					}

					//Log.d("info", "Loop");
				}
			}
		}
	};
}
