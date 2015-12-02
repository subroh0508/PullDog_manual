package kei.balloon.pulldog;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.beardedhen.androidbootstrap.BootstrapButton;

/**
 * Created by 拓海 on 2015/09/06.
 */
public class RouteMakeManual extends Activity implements GestureDetector.OnGestureListener{
    private GestureDetector mGestureDetector;
    private ViewFlipper mViewFlipper;
    private TextView tvPage;
    final static int index = 8;
    private int page;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_make_manual);

        mGestureDetector = new GestureDetector(this, this);
        mViewFlipper = (ViewFlipper)findViewById(R.id.viewflipper);
        tvPage = (TextView)findViewById(R.id.page);
        page = 1;

        BootstrapButton cancel = (BootstrapButton)findViewById(R.id.cancel);
        BootstrapButton ok = (BootstrapButton)findViewById(R.id.ok);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent e1 ,MotionEvent e2, float velocityX ,float velocityY) {
        // 絶対値の取得
        float dx = Math.abs(velocityX);
        float dy = Math.abs(velocityY);
        // 指の移動方向(縦横)および距離の判定
        if (dx > dy && dx > 300) {
            // 指の移動方向(左右)の判定
            if (e1.getX() < e2.getX()) {
                mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
                mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));

                if(page != 1) {
                    mViewFlipper.showPrevious();
                    page--;
                }
            } else {
                mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
                mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));

                if(page != index) {
                    mViewFlipper.showNext();
                    page++;
                }
            }

            tvPage.setText(page+"/"+index);

            return true;
        }
        return false;
    }

}
