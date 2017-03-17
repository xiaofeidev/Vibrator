package com.github.xiaofei_dev.vibrator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private long mPressedTime = 0;
    //private Vibrator mVibrator;
    private VibratorUtil mVibratorUtil;
    private TextView hint;
    private LinearLayout bottomBar;
    private SeekBar mSeekBar;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private MyReceiver myReceiver;
    RemoteViews mRemoteViews;
    NotificationManager nm;
    Notification notification;

    private int vibrateMode;
    private int intensity;
    private int progress;
    private boolean isInApp;
    private boolean isChecked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(this);
        setContentView(R.layout.activity_main);


        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        vibrateMode = mSharedPreferences.getInt("MODE",VibratorUtil.INTERRUPT);
        isChecked = mSharedPreferences.getBoolean("IS_CHECKED",false);
        progress = mSharedPreferences.getInt("PROGRESS",5);
        setVibratePattern(progress);
        editor =mSharedPreferences.edit();

        //mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        mVibratorUtil = new VibratorUtil((Vibrator) getSystemService(Service.VIBRATOR_SERVICE));

        initViews();

        IntentFilter filter = new IntentFilter("android.intent.action.SCREEN_OFF");
        filter.addAction("com.github.xiaofei_dev.vibrator.action");
        filter.addAction("com.github.xiaofei_dev.vibrator.close");
        myReceiver = new MyReceiver();
        registerReceiver(myReceiver,filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myReceiver != null){
            unregisterReceiver(myReceiver);
            myReceiver = null;
            if(nm != null){
                nm.cancelAll();
            }
        }
    }

    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间

        if((mNowTime - mPressedTime) > 1000){//比较两次按键时间差
            Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        }
        else{
            //退出程序
            finish();
            System.exit(0);
        }
    }

    //加载菜单资源
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem item = menu.findItem(R.id.keep);
        item.setChecked(isChecked);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.keep:
                if(item.isChecked()){
                    isChecked = false;
                    item.setChecked(isChecked);
                    vibrateMode = VibratorUtil.INTERRUPT;
                    editor.putBoolean("IS_CHECKED",isChecked);
                    editor.putInt("MODE", vibrateMode);
                    setBottomBarVisibility();
                }else {
                    isChecked = true;
                    item.setChecked(isChecked);
                    vibrateMode = VibratorUtil.KEEP;
                    editor.putBoolean("IS_CHECKED",isChecked);
                    editor.putInt("MODE", vibrateMode);
                    setBottomBarVisibility();
                }
                editor.apply();
                break;
            case R.id.theme:
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.theme))
                        .setPositiveButton(getString(R.string.close),
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();
                dialog.setView(getColorPickerView(dialog));
                dialog.show();
                break;
            case R.id.setting:
                break;
            default:
                break;
        }
        return true;
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == "android.intent.action.SCREEN_OFF" && isInApp){
                mVibratorUtil.vibrate(vibrateMode);
            }else if(intent.getAction() == "com.github.xiaofei_dev.vibrator.action"
                    && mVibratorUtil.isVibrate())
            {
                isInApp = false;
                mVibratorUtil.stopVibrate();
                hint.setText(R.string.start_vibrate);
                setBottomBarVisibility();
                mRemoteViews.setTextViewText(R.id.action,getString(R.string.remote_start_vibrate));
            }else if(intent.getAction() == "com.github.xiaofei_dev.vibrator.action"
                    && !mVibratorUtil.isVibrate())
            {
                isInApp = true;
                mVibratorUtil.vibrate(vibrateMode);
                hint.setText(R.string.stop_vibrate);
                //bottomBar.setVisibility(View.GONE);
                setBottomBarVisibility();
                mRemoteViews.setTextViewText(R.id.action,getString(R.string.remote_stop_vibrate));
            }else if(intent.getAction() == "com.github.xiaofei_dev.vibrator.close"){
                    isInApp = false;
                    mVibratorUtil.stopVibrate();
                    hint.setText(R.string.start_vibrate);
                    setBottomBarVisibility();
                    nm.cancelAll();
            }
            nm.notify(0, notification);
        }
    }

    private void setVibratePattern(int duration){
        VibratorUtil.mPattern[1] = duration * 20;
        VibratorUtil.mPattern[2] = duration * 4;
    }

    private void initViews(){
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_sort_white_36dp,null));
        setSupportActionBar(mToolbar);

        bottomBar = (LinearLayout) findViewById(R.id.bottom_bar);
        setBottomBarVisibility();

        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setProgress(progress);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                intensity = progress;
                editor.putInt("PROGRESS",intensity);
                editor.apply();
                setVibratePattern(intensity);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        hint = (TextView)findViewById(R.id.vibrate);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mVibratorUtil.isVibrate()){
                    isInApp = true;
                    mVibratorUtil.vibrate(vibrateMode);
                    hint.setText(R.string.stop_vibrate);
                    //bottomBar.setVisibility(View.GONE);
                    setBottomBarVisibility();
                    sendNotification();
                    mRemoteViews.setTextViewText(R.id.action,getString(R.string.remote_stop_vibrate));
                }else{
                    isInApp = false;
                    mVibratorUtil.stopVibrate();
                    hint.setText(R.string.start_vibrate);
                    //bottomBar.setVisibility(View.VISIBLE);
                    setBottomBarVisibility();
                    //nm.cancelAll();
                    mRemoteViews.setTextViewText(R.id.action,getString(R.string.remote_start_vibrate));
                }
                nm.notify(0, notification);
            }
        });
    }

    private void setBottomBarVisibility(){
        if(mVibratorUtil.isVibrate() || isChecked){
            bottomBar.setVisibility(View.GONE);
        }else {
            bottomBar.setVisibility(View.VISIBLE);
        }
    }

    private View getColorPickerView(final AlertDialog dialog) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.color_picker, null);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor spe = getSharedPreferences("item", MODE_PRIVATE).edit();
                switch (v.getId()) {
                    case R.id.magenta:
                        spe.putInt("theme",R.style.AppTheme);
                        break;
                    case R.id.red:
                        spe.putInt("theme",R.style.AppTheme_Red);
                        break;
                    case R.id.pink:
                        spe.putInt("theme",R.style.AppTheme_Pink);
                        Log.d(TAG, "onClick: pink");
                        break;
                    case R.id.yellow:
                        spe.putInt("theme",R.style.AppTheme_Yellow);
                        Log.d(TAG, "onClick: yellow");
                        break;
                    default:
                        break;
                }

                spe.apply();
                dialog.cancel();
                restart();
            }
        };

        rootView.findViewById(R.id.magenta).setOnClickListener(clickListener);
        rootView.findViewById(R.id.red).setOnClickListener(clickListener);
        rootView.findViewById(R.id.pink).setOnClickListener(clickListener);
        rootView.findViewById(R.id.yellow).setOnClickListener(clickListener);

        return rootView;
    }

    //设置主题
    public static void setTheme(Context context) {

        SharedPreferences preferences = context.getSharedPreferences("item", MODE_PRIVATE);
        int themeId = preferences.getInt("theme", 0);

        switch (themeId) {
            case R.style.AppTheme_Red:
                context.setTheme(themeId);
                break;
            case R.style.AppTheme_Pink:
                context.setTheme(themeId);
                break;
            case R.style.AppTheme_Yellow:
                context.setTheme(themeId);
                break;
            default:
                context.setTheme(R.style.AppTheme);
                break;
        }
    }

    private void restart() {
        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification);

        PendingIntent contral = PendingIntent.getBroadcast(this, 0,
                new Intent("com.github.xiaofei_dev.vibrator.action"),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.action,contral);

        PendingIntent close = PendingIntent.getBroadcast(this,1,
                new Intent("com.github.xiaofei_dev.vibrator.close"),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.close,close);

        builder.setContent(mRemoteViews)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = builder.build();
        nm.notify(0, notification);
    }

}
