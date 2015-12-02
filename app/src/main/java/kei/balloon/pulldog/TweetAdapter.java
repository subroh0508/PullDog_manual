package kei.balloon.pulldog;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Created by kei on 2015/08/16.
 */
public class TweetAdapter extends ArrayAdapter<String> {
    public TweetAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
    }
}
