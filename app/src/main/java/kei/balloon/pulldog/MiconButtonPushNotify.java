package kei.balloon.pulldog;

import android.widget.TextView;


/**
 * Created by kei on 2015/08/19.
 */
public class MiconButtonPushNotify {

    private TextView textV = null;
    private MiconButtonPushListener listener = null;

    public MiconButtonPushNotify(TextView TV) {
        this.textV = TV;
    }


    /**
     * EditTextにテキストが入力されているかどうかを判定する
     */
    public void checkText(){
        if(this.listener != null){
            if(this.textV.getText().toString().equals("MICON_BUTTON_SHORT_PUSH_SW1")){
                // TextViewにマイコンのSW1が短く押されたことを示すStringが入ったら
                listener.shortPushSW1();
            }else if(this.textV.getText().toString().equals("MICON_BUTTON_SHORT_PUSH_SW2")){
                // TextViewにマイコンのSW2が短く押されたことを示すStringが入ったら
                listener.shortPushSW2();
            }else if(this.textV.getText().toString().equals("MICON_BUTTON_LONG_PUSH_SW1")){
                // TextViewにマイコンのSW1が長く押されたことを示すStringが入ったら
                listener.longPushSW1();
            }else if(this.textV.getText().toString().equals("MICON_BUTTON_LONG_PUSH_SW2")){
                // TextViewにマイコンのSW2が長く押されたことを示すStringが入ったら
                listener.longPushSW2();
            }
        }
    }

    /**
     * リスナーを追加する
     * @param listener
     */
    public void setListener(MiconButtonPushListener listener){
        this.listener = listener;
    }

    /**
     * リスナーを削除する
     */
    public void removeListener(){
        this.listener = null;
    }
}
