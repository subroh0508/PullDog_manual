package kei.balloon.pulldog;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import android.widget.Toast;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by kei on 2015/08/16.
 */
public class TweetActivity extends FragmentActivity {

    private EditText mInputText;
    private String str;
    private double[] point = {0.0,0.0,0.0};
    private Twitter mTwitter;
    private StatusUpdate status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        mTwitter = TwitterUtils.getTwitterInstance(this);

        mInputText = (EditText) findViewById(R.id.input_text);

        str = getIntent().getStringExtra(getString(R.string.twitter_putExtra_key));         //つぶやきを取得

        point = getIntent().getDoubleArrayExtra(getString(R.string.user_latlng));                             //現在地

        status = new StatusUpdate(str + " #PULLDOG " + point[0] + "," + point[1]);

        mInputText.setText(str + " #PULLDOG ");

        tweet(status);      //つぶやく


    }

    private void tweet(StatusUpdate tweetStr) {
        AsyncTask<StatusUpdate,Void,Boolean> task = new AsyncTask<StatusUpdate,Void,Boolean>() {
            @Override
            protected Boolean doInBackground(StatusUpdate... params) {
                try {
                    mTwitter.updateStatus(params[0]);
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Intent data = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("res", 1);
                    data.putExtras(bundle);
                    setResult(RESULT_OK, data);

                    finish();
                } else {
                    Intent data = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("res", 0);
                    data.putExtras(bundle);
                    setResult(RESULT_OK, data);

                    finish();
                }
            }
        };
        task.execute(tweetStr);

    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}