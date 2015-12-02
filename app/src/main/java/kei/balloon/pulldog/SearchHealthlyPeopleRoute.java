package kei.balloon.pulldog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapEditText;

/**
 * Created by kei on 2015/09/16.
 */
public class SearchHealthlyPeopleRoute extends Activity {

    private ListView searchList;
    private DataAccess da;
    private BootstrapEditText idETxt;
    private InputMethodManager inputMethodManager;
    private LinearLayout lLayout;
    private EditText ed;
    private SearchHealthlyPeopleRoute ac;
    public static TextView txt;
    private int routeId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthly_route_search);

        ac = this;

        //キーボード表示を制御するためのオブジェクト
        inputMethodManager =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        txt = new TextView(this);
        lLayout = (LinearLayout) findViewById(R.id.not_found_linear);
        ed = (EditText) findViewById(R.id.dummyEditText);
        idETxt = (BootstrapEditText) findViewById(R.id.search_id);
        searchList = (ListView) findViewById(R.id.route_list);
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ここに処理を書く
                routeId = ((RouteData) parent.getAdapter().getItem(position)).getid();
                da.fileGet(String.valueOf(((RouteData) parent.getAdapter().getItem(position)).getid()));
            }
        });
        da = new DataAccess(this,searchList,lLayout);
        idETxt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //イベントを取得するタイミングには、ボタンが押されてなおかつエンターキーだったときを指定
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //キーボードを閉じる
                    inputMethodManager.hideSoftInputFromWindow(idETxt.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    ed.requestFocus();
                    da.fileSearch(idETxt.getText().toString());
                    return true;
                }
                return false;
            }
        });
        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Intent intent = new Intent(ac,SearchHealthlyPeopleRoutePreview.class);
                intent.putExtra("ROUTE",txt.getText().toString());
                intent.putExtra("ID",routeId);
                startActivity(intent);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }
}
