package com.github.xiaofei_dev.vibrator.ui

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.widget.CheckBox
import android.widget.RemoteViews
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.xiaofei_dev.vibrator.App
import com.github.xiaofei_dev.vibrator.R
import com.github.xiaofei_dev.vibrator.extension.setTheme
import com.github.xiaofei_dev.vibrator.extension.yes
import com.github.xiaofei_dev.vibrator.singleton.AppStatus
import com.github.xiaofei_dev.vibrator.singleton.Preference
import com.github.xiaofei_dev.vibrator.singleton.Preference.isChecked
import com.github.xiaofei_dev.vibrator.singleton.Preference.mProgress
import com.github.xiaofei_dev.vibrator.singleton.Preference.mPurchaseStatus
import com.github.xiaofei_dev.vibrator.singleton.Preference.mTheme
import com.github.xiaofei_dev.vibrator.singleton.Preference.mVibrateMode
import com.github.xiaofei_dev.vibrator.singleton.PurchaseStatus
import com.github.xiaofei_dev.vibrator.util.BillingLogic
import com.github.xiaofei_dev.vibrator.util.ToastUtil
import com.github.xiaofei_dev.vibrator.util.VibratorUtil
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.find


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

    private var mPendingIntentFlag = PendingIntent.FLAG_UPDATE_CURRENT

    private val mBillingLogic = BillingLogic(lifecycleScope) {
        mPurchaseStatus = PurchaseStatus.BOUGHT
        destroyAdView()
        if(this@MainActivity::mMenu.isInitialized){
            if (mMenu.findItem(R.id.removead) != null){
                mMenu.removeItem(R.id.removead)
            }
        }
    }

    private var isAdViewDestroyed = false

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                sendNotification()
            } else {

            }
        }

    val onBackPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            /*val mNowTime = System.currentTimeMillis()//记录本次按键时刻
            if (mNowTime - mPressedTime > 2000) {//比较两次按键时间差
                Toast.makeText(this@MainActivity, R.string.quit_hint, Toast.LENGTH_SHORT).show()
                mPressedTime = mNowTime
            } else {
                //退出程序
                if (mVibratorUtil?.isVibrate?:false) {
                    isInApp = false
                    mVibratorUtil?.stopVibrate()
                    textAction.setText(R.string.start_vibrate)
                    setBottomBarVisibility()
                    mAnimator?.cancel()
                }
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }*/
            //退出程序
            if (mVibratorUtil?.isVibrate?:false) {
                isInApp = false
                mVibratorUtil?.stopVibrate()
                textAction.setText(R.string.start_vibrate)
                setBottomBarVisibility()
                mAnimator?.cancel()
            }
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()

            /*if (mPurchaseStatus != PurchaseStatus.BOUGHT && mInterstitialAd != null && !isShowed) {
                //展示广告
                showAD()
            } else {
                //直接退出程序
                if (mVibratorUtil?.isVibrate?:false) {
                    isInApp = false
                    mVibratorUtil?.stopVibrate()
                    textAction.setText(R.string.start_vibrate)
                    setBottomBarVisibility()
                    mAnimator?.cancel()
                }
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }*/
        }
    }

    /*private var mInterstitialAd: InterstitialAd? = null
    private var isShowed = false
    private fun loadAd(retryTime: Int) {
        if (mPurchaseStatus == PurchaseStatus.BOUGHT){
            return
        }
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,getString(R.string.inter_id_release1), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                //Log.d(TAG, adError?.toString())
                mInterstitialAd = null
                if (retryTime > 0 && !isFinishing) {
                    loadAd(retryTime - 1)
                }
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                //广告加载成功
                if (!isFinishing) {
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            //Log.d(TAG, "Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            //Log.d(TAG, "Ad dismissed fullscreen content.")
                            //mInterstitialAd = null
                            //广告被用户手动关闭？退出应用
                            isShowed = true
                            onBackPressedDispatcher.onBackPressed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            // Called when ad fails to show.
                            //Log.e(TAG, "Ad failed to show fullscreen content.")
                            //mInterstitialAd = null
                            //广告展示错误？退出应用
                            isShowed = true
                            onBackPressedDispatcher.onBackPressed()
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            //Log.d(TAG, "Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            //Log.d(TAG, "Ad showed fullscreen content.")
                        }
                    }
                }
            }
        })
    }

    private fun showAD() {
        if (mPurchaseStatus == PurchaseStatus.BOUGHT){
            return
        }
        if (mInterstitialAd != null && !isFinishing) {
            mInterstitialAd?.show(this)
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //loadAd(3)
        //设置应用主题
        setTheme()
        setContentView(R.layout.activity_main)
        checkPurchaseStatus()
        initAd()
        if (Build.VERSION.SDK_INT >= 31){
            mPendingIntentFlag = mPendingIntentFlag or PendingIntent.FLAG_IMMUTABLE
        }

        mIntensity = 40 - mProgress
        if(mIntensity <= 0){
            mIntensity = 1
        }
        setVibratePattern(mIntensity)

        if (Build.VERSION.SDK_INT >= 31){
            mVibratorUtil = VibratorUtil((getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator)
        } else{
            mVibratorUtil = VibratorUtil(getSystemService(Service.VIBRATOR_SERVICE) as Vibrator)
        }
        AppStatus.mNotThemeChange = true
        //初始化 View
        initViews()

        val filter = IntentFilter("android.intent.action.SCREEN_OFF")
        filter.addAction("com.github.xiaofei_dev.vibrator.action")
        filter.addAction("com.github.xiaofei_dev.vibrator.close")
        mMyRecever = MyReceiver()
        registerReceiver(mMyRecever, filter)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onResume() {
        super.onResume()
        if (mPurchaseStatus != PurchaseStatus.BOUGHT) {
            lifecycleScope.launch {
                mBillingLogic.checkActiveBillingCheckReady(this@MainActivity){
                    mPurchaseStatus = PurchaseStatus.BOUGHT
                    destroyAdView()
                    if(this@MainActivity::mMenu.isInitialized){
                        if (mMenu.findItem(R.id.removead) != null){
                            mMenu.removeItem(R.id.removead)
                        }
                    }
                }
            }
        }
    }

    //检查用户的应用内购状态
    private fun checkPurchaseStatus(){
        mBillingLogic.init(this)
        when (mPurchaseStatus) {
            PurchaseStatus.UNKNOWN -> {
                lifecycleScope.launch {
                    mBillingLogic.isBoughtCheckReady(
                        this@MainActivity,
                        tureAction = {
                            mPurchaseStatus = PurchaseStatus.BOUGHT
                            destroyAdView()
                            if(this@MainActivity::mMenu.isInitialized){
                                if (mMenu.findItem(R.id.removead) != null){
                                    mMenu.removeItem(R.id.removead)
                                }
                            }
                        },
                        falseAction = {
                            mPurchaseStatus = PurchaseStatus.NO_BOUGHT
                        }
                    )
                }
            }
            PurchaseStatus.BOUGHT -> {//即使当前状态是已购买，也有可能在 play console 中被退款而移除商品拥有权限
                destroyAdView()
                if(this@MainActivity::mMenu.isInitialized){
                    if (mMenu.findItem(R.id.removead) != null){
                        mMenu.removeItem(R.id.removead)
                    }
                }

                //冗余的检查以应对发生退款之类的情况
                lifecycleScope.launch {
                    mBillingLogic.isBoughtCheckReady(
                        this@MainActivity,
                        tureAction = {},
                        falseAction = {
                            mPurchaseStatus = PurchaseStatus.NO_BOUGHT
                        })
                }
            }
            else -> {//PurchaseStatus.NO_BOUGHT
                //什么都不用做？
            }
        }
    }

    //加载广告
    private fun initAd(){
        if (mPurchaseStatus == PurchaseStatus.BOUGHT){
            return
        }
        //初始化 AdMob
        if (App.adState != AdapterStatus.State.READY){
            MobileAds.initialize(this) {
                it.adapterStatusMap.get(MobileAds::class.qualifiedName)?.initializationState?.let {
                    App.adState = it
                    loadAd()
                }
            }
        } else {
            loadAd()
        }
    }

    private fun loadAd(){
        if (mPurchaseStatus == PurchaseStatus.BOUGHT){
            return
        }
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun destroyAdView() {
        if (adView != null && !isAdViewDestroyed) {
            adView.visibility = GONE
            adView.destroy()
            isAdViewDestroyed = true
        }
    }

    override fun onDestroy() {
        mNotification = null
        if (nm != null && AppStatus.mNotThemeChange) {
            nm?.cancelAll()
        }
        if (mMyRecever != null) {
            unregisterReceiver(mMyRecever)
            mMyRecever = null
        }
        mAnimator?.cancel()
        super.onDestroy()
    }


    private lateinit var mMenu: Menu
    //加载菜单资源
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        mMenu = menu
        if (mPurchaseStatus == PurchaseStatus.BOUGHT){
            mMenu.removeItem(R.id.removead)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.removead -> {
                //购买去除广告
                mBillingLogic.billingConnect(this)
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
            R.id.about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        return true
    }

    //更新通知相关
    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.intent.action.SCREEN_OFF" && isInApp) {
                mVibratorUtil?.vibrate(mVibrateMode)
            } else if (intent.action == "com.github.xiaofei_dev.vibrator.action" && mVibratorUtil?.isVibrate?:false) {
                isInApp = false
                mVibratorUtil?.stopVibrate()
                textAction.setText(R.string.start_vibrate)
                mAnimator?.cancel()
                setBottomBarVisibility()
                mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_start_vibrate))
                mNotification?.let {//更新通知
                    nm?.notify(0, it)
                }
            } else if (intent.action == "com.github.xiaofei_dev.vibrator.action" && !(mVibratorUtil?.isVibrate?:false)) {
                isInApp = true
                mVibratorUtil?.vibrate(mVibrateMode)
                textAction.setText(R.string.stop_vibrate)
                mAnimator?.start()
                setBottomBarVisibility()
                mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_stop_vibrate))
                mNotification?.let {//更新通知
                    nm?.notify(0, it)
                }
            } else if (intent.action == "com.github.xiaofei_dev.vibrator.close") {
                isInApp = false
                mVibratorUtil?.stopVibrate()
                textAction.setText(R.string.start_vibrate)
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
        setSupportActionBar(toolbar)
        //setBottomBarVisibility()

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

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        //CheckBox 设置
        isKeep?.isChecked = isChecked
        isKeep?.setOnClickListener { v ->
            if (v is CheckBox){
                isChecked = v.isChecked
                mVibrateMode = if (v.isChecked){
                    VibratorUtil.KEEP
                } else {
                    VibratorUtil.INTERRUPT
                }
            }
        }

        //发出去通知
        if(Build.VERSION.SDK_INT >= 24){
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.areNotificationsEnabled()) {
                // Permission granted
                sendNotification()
            } else {
                // Permission not granted
                if (Build.VERSION.SDK_INT >= 33){
                    when {
                        ContextCompat.checkSelfPermission(this,
                            Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                                // You can use the API that requires the permission.
                                sendNotification()
                        }
                        else -> {
                            // You can directly ask for the permission.
                            // The registered ActivityResultCallback gets the result of this request.
                            requestPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
            }
        } else{
            sendNotification()
        }

        textAction.setOnClickListener {
            if (!(mVibratorUtil?.isVibrate?:false)) {
                isInApp = true
                mVibratorUtil?.vibrate(mVibrateMode)
                textAction.setText(R.string.stop_vibrate)
                setBottomBarVisibility()
                mAnimator?.start()
                mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_stop_vibrate))
            } else {
                isInApp = false
                mVibratorUtil?.stopVibrate()
                textAction.setText(R.string.start_vibrate)
                setBottomBarVisibility()
                mAnimator?.cancel()
                mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_start_vibrate))
            }
            mNotification?.let {//更新通知
                nm?.notify(0, it)
            }
        }
        ////////////////发通知结束
        mAnimator = AnimatorInflater.loadAnimator(this@MainActivity, R.animator.anim_vibrate)
        mAnimator?.setTarget(textAction)
    }

    private fun setBottomBarVisibility() {
        if (mVibratorUtil?.isVibrate?:false) {
            bottomBar.visibility = View.GONE
        } else {
            bottomBar.visibility = View.VISIBLE
        }
    }
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
                R.id.black -> mTheme = R.style.AppTheme_Black
            }
            dialog.cancel()
            window.setWindowAnimations(R.style.WindowAnimationFadeInOut)
            //recreate()
            setTheme()
            recreate();

            /*val contextThemeWrapper = ContextThemeWrapper(this@MainActivity, mTheme)
            val inflater = LayoutInflater.from(contextThemeWrapper)
            val view = inflater.inflate(R.layout.activity_main, null)
            setContentView(view)*/

            AppStatus.mNotThemeChange = false
        }

        rootView.find<View>(R.id.magenta).setOnClickListener(clickListener)
        rootView.find<View>(R.id.red).setOnClickListener(clickListener)
        rootView.find<View>(R.id.pink).setOnClickListener(clickListener)
        rootView.find<View>(R.id.yellow).setOnClickListener(clickListener)
        rootView.find<View>(R.id.green).setOnClickListener(clickListener)
        rootView.find<View>(R.id.blue).setOnClickListener(clickListener)
        rootView.find<View>(R.id.black).setOnClickListener(clickListener)

        return rootView
    }

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
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    private fun sendNotification() {
        initChannels()
        val builder = NotificationCompat.Builder(this, "default")
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val intent = PendingIntent.getActivity(this, 0, i,
            mPendingIntentFlag)

        mRemoteViews = RemoteViews(packageName, R.layout.layout_notification)

        val control = PendingIntent.getBroadcast(this, 0,
                Intent("com.github.xiaofei_dev.vibrator.action"),
            mPendingIntentFlag)
        mRemoteViews?.setOnClickPendingIntent(R.id.action, control)

        val close = PendingIntent.getBroadcast(this, 1,
                Intent("com.github.xiaofei_dev.vibrator.close"),
            mPendingIntentFlag)
        mRemoteViews?.setOnClickPendingIntent(R.id.close, close)

        builder.setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_vibration)
                .setSound(null)
                .setOnlyAlertOnce(true)//成功使通知声音只响一次！
                .setOngoing(true)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(mRemoteViews)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .priority = NotificationCompat.PRIORITY_HIGH


        nm = NotificationManagerCompat.from(this)
        mNotification = builder.build()

        mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_start_vibrate))
        mNotification?.let {//更新通知
            nm?.notify(0, it)
        }
    }
}
