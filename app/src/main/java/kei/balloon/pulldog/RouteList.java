package kei.balloon.pulldog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 拓海 on 2015/09/16.
 */
public class RouteList extends BaseAdapter {

    private List<RouteData> dataList;
    private Context context;
    private LayoutInflater layoutInflater;

    RouteList(List list, Context con){
        super();
        dataList = list;
        context  = con;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView name;
        TextView id;
        TextView totalDistance;
        RouteData item = (RouteData)getItem(position);

        // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
        if (null == convertView) {
            convertView = layoutInflater.inflate(R.layout.route_object, null);
        }

        name = (TextView)convertView.findViewById(R.id.route_name);
        name.setText(String.valueOf(item.getName()));
        id = (TextView)convertView.findViewById(R.id.route_id);
        id.setText("ID:"+String.valueOf(item.getid()));
        totalDistance = (TextView)convertView.findViewById(R.id.route_total_distance);
        totalDistance.setText("距離:"+String.valueOf(item.getDistance())+"m");

        return convertView;
    }
}
