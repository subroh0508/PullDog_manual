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
	private final static int RED = 0x66FF0000, ORANGE = 0x66FF6600, YELLOW = 0xFFFFFF00,
			GREEN = 0x6600FF00, BLUE = 0x660000FF, WHITE = 0x00FFFFFF, BLACK = 0xFF000000;

	private Bundle infoBundle = null;
	private Qzss qzssInfo = null;
	private Handler infoHandler = null;
	private MainActivity activity;

	private TextView sateliteCount, gnssLog, gnssOn, qzssOn, l1saifOn;
	private EditText fileName;
	private Button convertCsv;
	private boolean threadIsStopped = true;

	private ConvertCsvToKml csvToKml, gnssCsvToKml, rfidCsvToKml;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.info_fragment, container, false);

		sateliteCount = (TextView)rootView.findViewById(R.id.satelite_count);
		gnssLog = (TextView)rootView.findViewById(R.id.gnss_log_view);
		gnssOn = (TextView)rootView.findViewById(R.id.glonass_on);
		qzssOn = (TextView)rootView.findViewById(R.id.qzss_on);
		l1saifOn = (TextView)rootView.findViewById(R.id.l1saif_on);

		fileName = (EditText)rootView.findViewById(R.id.filename_csv);
		convertCsv = (Button)rootView.findViewById(R.id.convert_csv_to_kml);

		gnssOn.setBackgroundColor(WHITE);
		qzssOn.setBackgroundColor(WHITE);
		qzssOn.setBackgroundColor(WHITE);

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
				gnssCsvToKml = new ConvertCsvToKml(fileName.getText().toString()+"_gnss");
				rfidCsvToKml = new ConvertCsvToKml(fileName.getText().toString()+"_rfid");

				if(csvToKml.convert() && gnssCsvToKml.convert() && rfidCsvToKml.convert())
					convertCsv.setText("SUCCESS!");
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
							int visible = 0, useful = 0;

							visible = qzssInfo.getVisibleCount();
							useful = qzssInfo.getUsefulCount();

							gnssLog.setText(qzssInfo.getLog() + "\n");

							sateliteCount.setText("visible:" + visible + "  useful:" + useful);
							updateTextView();
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

	private void updateTextView(){
		if(qzssInfo.GLONASS_ON)
			gnssOn.setBackgroundColor(YELLOW);
		else
			gnssOn.setBackgroundColor(WHITE);

		if(qzssInfo.QZSS_ON)
			qzssOn.setBackgroundColor(GREEN);
		else
			qzssOn.setBackgroundColor(WHITE);

		if(qzssInfo.L1SAIF_ON)
			qzssOn.setBackgroundColor(BLUE);
		else
			qzssOn.setBackgroundColor(WHITE);


	}
}
