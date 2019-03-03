package com.github.xiaofei_dev.vibrator.ui

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RemoteViews
import android.widget.SeekBar
import android.widget.Toast
import com.github.xiaofei_dev.vibrator.R
import com.github.xiaofei_dev.vibrator.extension.yes
import com.github.xiaofei_dev.vibrator.singleton.AppStatus
import com.github.xiaofei_dev.vibrator.singleton.Preference
import com.github.xiaofei_dev.vibrator.singleton.Preference.isChecked
import com.github.xiaofei_dev.vibrator.singleton.Preference.mProgress
import com.github.xiaofei_dev.vibrator.singleton.Preference.mTheme
import com.github.xiaofei_dev.vibrator.singleton.Preference.mVibrateMode
import com.github.xiaofei_dev.vibrator.util.ToastUtil
import com.github.xiaofei_dev.vibrator.util.VibratorUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.notificationManager

class MainActivity : AppCompatActivity() {
    private var mPressedTime: Long = 0
    private var mVibratorUtil: VibratorUtil? = null
    private var mMyRecever: MyReceiver? = null
    private var mRemoteViews: RemoteViews? = null
    private var nm: NotificationManagerCompat? = null
    private var mNotification: Notification? = null

    private var mIntensity: Int = 0
    private var isInApp: Boolean = false
    private var mAnimator: Animator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(this)
        setContentView(R.layout.activity_main)

        mIntensity = 40 - mProgress
        if(mIntensity <= 0){
            mIntensity = 1
        }
        setVibratePattern(mIntensity)
        //mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        mVibratorUtil = VibratorUtil(getSystemService(Service.VIBRATOR_SERVICE) as Vibrator)
        AppStatus.mNotThemeChange = true
        initViews()

        val filter = IntentFilter("android.intent.action.SCREEN_OFF")
        filter.addAction("com.github.xiaofei_dev.vibrator.action")
        filter.addAction("com.github.xiaofei_dev.vibrator.close")
        mMyRecever = MyReceiver()
        registerReceiver(mMyRecever, filter)
        Log.d(TAG, "onCreate: ")
    }

    override fun onDestroy() {
        mNotification = null
        if (nm != null && AppStatus.mNotThemeChange) {
            nm?.cancelAll()
        }
        //        unregisterReceiver(mMyRecever);
        if (mMyRecever != null) {
            unregisterReceiver(mMyRecever)
            mMyRecever = null
        }
        //        if(mAnimator.isStarted()){
        //            mAnimator.cancel();
        //        }
        mAnimator?.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        val mNowTime = System.currentTimeMillis()//记录本次按键时刻
        if (mNowTime - mPressedTime > 2000) {//比较两次按键时间差
            Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show()
            mPressedTime = mNowTime
        } else {
            //退出程序
            if (mVibratorUtil?.isVibrate?:false) {
                isInApp = false
                mVibratorUtil?.stopVibrate()
                textHint.setText(R.string.start_vibrate)
                setBottomBarVisibility()
                mAnimator?.cancel()
            }
            super.onBackPressed()
        }
    }

    //加载菜单资源
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val item = menu.findItem(R.id.keep)
        item.isChecked = isChecked
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.keep -> {
                if (item.isChecked) {
                    isChecked = false
                    item.isChecked = isChecked
                    mVibrateMode = VibratorUtil.INTERRUPT
                    setBottomBarVisibility()
                } else {
                    isChecked = true
                    item.isChecked = isChecked
                    mVibrateMode = VibratorUtil.KEEP
                    setBottomBarVisibility()
                }
            }
            R.id.theme -> {
                val dialog = AlertDialog.Builder(this, R.style.Dialog)
                        .setTitle(getString(R.string.theme))
                        .setPositiveButton(getString(R.string.close)) {
                            dialog, which -> dialog.cancel()
                        }
                        .create()
                dialog.setView(getColorPickerView(dialog))
                dialog.show()
            }
            /*R.id.discover ->{
                val dialog = AlertDialog.Builder(this, R.style.Dialog)
                        *//*.setTitle(getString(R.string.discover))
                        .setPositiveButton(getString(R.string.close)) {
                            dialog, which -> dialog.cancel()
                        }*//*
                        .create()
                dialog.setView(getDiscoverView(dialog))
                dialog.show()
                //得到这个dialog界面的参数对象
                val params:WindowManager.LayoutParams = dialog.getWindow().getAttributes();
                //设置dialog的界面宽度
                params.width = screenWidth/5 *4
                //设置dialog高度为包裹内容
                params.height =  WindowManager.LayoutParams.WRAP_CONTENT;
                //设置dialog的重心
                params.gravity = Gravity.CENTER;
                dialog.window.attributes = params
            }*/
            R.id.about -> startActivity(Intent(this, AboutActivity::class.java))
            else -> {
            }
        }
        return true
    }

    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.intent.action.SCREEN_OFF" && isInApp) {
                mVibratorUtil?.vibrate(mVibrateMode)
            } else if (intent.action == "com.github.xiaofei_dev.vibrator.action" && mVibratorUtil?.isVibrate?:false) {
                isInApp = false
                mVibratorUtil?.stopVibrate()
                textHint.setText(R.string.start_vibrate)
                mAnimator?.cancel()
                setBottomBarVisibility()
                mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_start_vibrate))
                mNotification?.let {//更新通知
                    nm?.notify(0, it)
                }
            } else if (intent.action == "com.github.xiaofei_dev.vibrator.action" && !(mVibratorUtil?.isVibrate?:false)) {
                isInApp = true
                mVibratorUtil?.vibrate(mVibrateMode)
                textHint.setText(R.string.stop_vibrate)
                mAnimator?.start()
                setBottomBarVisibility()
                mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_stop_vibrate))
                mNotification?.let {//更新通知
                    nm?.notify(0, it)
                }
            } else if (intent.action == "com.github.xiaofei_dev.vibrator.close") {
                isInApp = false
                mVibratorUtil?.stopVibrate()
                textHint.setText(R.string.start_vibrate)
                mAnimator?.cancel()
                setBottomBarVisibility()
                nm?.cancelAll()
            }
        }
    }

    private fun setVibratePattern(duration: Int) {
        VibratorUtil.mPattern[1] = (duration * 16).toLong()
        VibratorUtil.mPattern[2] = (duration * 4).toLong()
    }

    private fun initViews() {
        //        mToolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_sort_white_36dp,null));
        setSupportActionBar(toolbar)
        setBottomBarVisibility()

        seekBar.progress = mProgress
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, mProgress: Int, fromUser: Boolean) {
                mIntensity = 40 - mProgress
                if(mIntensity <= 0){
                    mIntensity = 1
                }
                Preference.mProgress = mProgress
                setVibratePattern(mIntensity)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        //发出去通知
        sendNotification()
        mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_start_vibrate))
        mNotification?.let {//更新通知
            nm?.notify(0, it)
        }
        textHint.setOnClickListener {
            if (!(mVibratorUtil?.isVibrate?:false)) {
                isInApp = true
                mVibratorUtil?.vibrate(mVibrateMode)
                textHint.setText(R.string.stop_vibrate)
                //seekBar.setVisibility(View.GONE);
                setBottomBarVisibility()
                mAnimator?.start()
                //                    sendNotification();
                mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_stop_vibrate))
            } else {
                isInApp = false
                mVibratorUtil?.stopVibrate()
                textHint.setText(R.string.start_vibrate)
                //seekBar.setVisibility(View.VISIBLE);
                setBottomBarVisibility()
                mAnimator?.cancel()
                //nm.cancelAll();
                mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_start_vibrate))
            }
            mNotification?.let {//更新通知
                nm?.notify(0, it)
            }
        }
        ////////////////发通知结束
        mAnimator = AnimatorInflater.loadAnimator(this@MainActivity, R.animator.anim_vibrate)
        mAnimator?.setTarget(textHint)
    }

    private fun setBottomBarVisibility() {
        if (mVibratorUtil?.isVibrate?:false || isChecked) {
            bottomBar.visibility = View.GONE
        } else {
            bottomBar.visibility = View.VISIBLE
        }
    }

    /*private fun getDiscoverView(dialog: AlertDialog): View{
        val rootView = layoutInflater.inflate(R.layout.layout_discover, null)
        *//*val layoutParams:ViewGroup.LayoutParams =
                ViewGroup.LayoutParams(dip(200), ViewGroup.LayoutParams.WRAP_CONTENT)
        rootView.layoutParams = layoutParams*//*
        return rootView
    }*/

    private fun getColorPickerView(dialog: AlertDialog): View {
        val rootView = layoutInflater.inflate(R.layout.layout_color_picker, null)
        val clickListener = View.OnClickListener { v ->
            //若正在震动则不允许切换主题
            mVibratorUtil?.let {
                it.isVibrate.yes {
                    ToastUtil.showToast(this@MainActivity, getString(R.string.not_allow))
                    return@OnClickListener
                }
            }
            when (v.id) {
                R.id.magenta -> mTheme = R.style.AppTheme
                R.id.red -> mTheme = R.style.AppTheme_Red
                R.id.pink -> mTheme = R.style.AppTheme_Pink
                R.id.yellow -> mTheme = R.style.AppTheme_Yellow
                R.id.green -> mTheme = R.style.AppTheme_Green
                R.id.blue -> mTheme = R.style.AppTheme_Blue
            }
            dialog.cancel()
            window.setWindowAnimations(R.style.WindowAnimationFadeInOut)
            recreate()
            AppStatus.mNotThemeChange = false
        }

        rootView.findViewById<View>(R.id.magenta).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.red).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.pink).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.yellow).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.green).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.blue).setOnClickListener(clickListener)

        return rootView
    }

    //    private void restart() {
    //        Intent intent = getIntent();
    //        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
    //        startActivity(intent);
    ////        finish();
    //    }

    private fun initChannels() {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val channel = NotificationChannel("default",
                "VibrateChannel",
                NotificationManager.IMPORTANCE_HIGH)
        channel.enableLights(false)
        channel.description = "按摩棒默认通知渠道"
        channel.setShowBadge(false)
        channel.setSound(null, null)
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendNotification() {
        initChannels()
        val builder = NotificationCompat.Builder(this, "default")
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val intent = PendingIntent.getActivity(this, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT)

        mRemoteViews = RemoteViews(packageName, R.layout.layout_notification)

        val control = PendingIntent.getBroadcast(this, 0,
                Intent("com.github.xiaofei_dev.vibrator.action"),
                PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteViews?.setOnClickPendingIntent(R.id.action, control)

        val close = PendingIntent.getBroadcast(this, 1,
                Intent("com.github.xiaofei_dev.vibrator.close"),
                PendingIntent.FLAG_UPDATE_CURRENT)
        mRemoteViews?.setOnClickPendingIntent(R.id.close, close)

        builder.setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_vibration)
                .setSound(null)
                .setOnlyAlertOnce(true)//成功使通知声音只响一次！
                .setOngoing(true)
                .setContent(mRemoteViews)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .priority = NotificationCompat.PRIORITY_DEFAULT


        nm = NotificationManagerCompat.from(this)
        mNotification = builder.build()
        /*mNotification?.let {
            nm?.notify(0, it)
        }*/
    }

    companion object {
        private val TAG = "MainActivity"
        //设置主题
        fun setTheme(context: Context) {
            when (mTheme) {
                R.style.AppTheme_Red,
                R.style.AppTheme_Pink,
                R.style.AppTheme_Yellow,
                R.style.AppTheme_Green,
                R.style.AppTheme_Blue -> context.setTheme(mTheme)
                else -> context.setTheme(R.style.AppTheme)
            }
        }
    }
}
