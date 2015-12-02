package kei.balloon.pulldog;

import java.util.EventListener;

/**
 * Created by kei on 2015/08/19.
 */
public interface MiconButtonPushListener extends EventListener{
    /**
     * SW1を短く押したとき
     */
    public void shortPushSW1();
    /**
     * SW2を短く押したとき
     */
    public void shortPushSW2();
    /**
     * SW3を長く押したとき
     */
    public void longPushSW1();
    /**
     * SW4を長く押したとき
     */
    public void longPushSW2();
}
