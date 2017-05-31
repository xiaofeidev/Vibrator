package com.github.xiaofei_dev.vibrator.util;

import android.os.Vibrator;
import android.util.Log;


/**
 * Created by Administrator on 2017/3/4.
 */
//此类用于描述振动属性，即Vibrator类的方法参数值
public final class VibratorUtil {
    private static final String TAG = "VibratorUtil";

    //振动模式为断续
    public static final int INTERRUPT = 0;

    //振动模式为持续
    public static final int KEEP = 1;

    private final Vibrator mVibrator;

    //通过设置24个小时时长来模拟持续不停地震动
    private long mDuration = 1000*60*60*10;
    public static long[] mPattern = {0,0,0};
    private final int mRepeat = 0;
    private boolean isVibrate;

    public VibratorUtil(Vibrator vibrator) {
        mVibrator = vibrator;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public boolean isVibrate() {
        return isVibrate;
    }

    private void setVibrate(boolean vibrate) {
        isVibrate = vibrate;
    }

    //开始震动
    public void vibrate(int mode){
        Log.d(TAG, "vibrate:0 ");
        setVibrate(true);
        switch (mode){
            case INTERRUPT:
                mVibrator.vibrate(mPattern,mRepeat);
                Log.d(TAG, "vibrate:0 ");
                break;
            case KEEP:
                mVibrator.vibrate(mDuration);
                break;
            default:
                break;
        }
    }

    public void stopVibrate(){
        setVibrate(false);
        mVibrator.cancel();
    }

}
