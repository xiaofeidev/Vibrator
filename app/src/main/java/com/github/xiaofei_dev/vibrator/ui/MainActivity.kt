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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.CheckBox
import android.widget.RemoteViews
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.xiaofei_dev.vibrator.App
import com.github.xiaofei_dev.vibrator.R
import com.github.xiaofei_dev.vibrator.extension.hideSystemUINew
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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import kotlinx.coroutines.launch
//import kotlinx.android.synthetic.main.activity_main.*
// 取消 Kotlin synthetic 导入，改用 findViewById
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdView
//import org.jetbrains.anko.find


class MainActivity : AppCompatActivity() {
    //private var mPressedTime: Long = 0
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

    // 视图引用，替代 Kotlin synthetic
    private lateinit var toolbar: Toolbar
    private lateinit var seekBar: SeekBar
    private lateinit var isKeep: CheckBox
    private lateinit var textAction: TextView
    private lateinit var bottomBar: View
    private lateinit var adView: AdView

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                sendNotification()
            } else {

            }
        }

//    var adShowing = false
//    var adLoaded = false
    val onBackPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            /*if (mPurchaseStatus != PurchaseStatus.BOUGHT && App.adState == AdapterStatus.State.READY && !adShowing){
                // 展示广告
                adShowing = true
                layoutAD.visibility = VISIBLE
                if (adLoaded) {
                    adView.resume()
                } else {
                    loadAd()
                }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //loadAd(3)
        //设置应用主题
        setTheme()
        setContentView(R.layout.activity_main)

        // 通过 findViewById 初始化视图
        toolbar = findViewById(R.id.toolbar)
        seekBar = findViewById(R.id.seekBar)
        isKeep = findViewById(R.id.isKeep)
        textAction = findViewById(R.id.textAction)
        bottomBar = findViewById(R.id.bottomBar)
        adView = findViewById(R.id.adView)

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
        // RemoteViews 中的 PendingIntent 由系统进程触发，属于跨进程广播，
        // 因此需要使用 RECEIVER_EXPORTED 才能在 Android 13+ 正常接收。
        // 注意：ContextCompat.registerReceiver 在所有系统版本都会强制要求提供
        // RECEIVER_EXPORTED 或 RECEIVER_NOT_EXPORTED 之一；低版本会忽略该值但不能为 0，
        // 否则会抛出 IllegalArgumentException。
        val receiverFlags = ContextCompat.RECEIVER_EXPORTED
//        val receiverFlags = 0
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//            // TIRAMISU 及以上可直接使用 3 参数版本，但为统一写法，此处改用 ContextCompat.registerReceiver
//        }

        // 始终通过 ContextCompat.registerReceiver 提供 flags，避免崩溃与 lint 检查
        ContextCompat.registerReceiver(this, mMyRecever, filter, receiverFlags)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onResume() {
        super.onResume()
        // onResume 是同步购买状态的最佳时机，因为它覆盖了启动和从后台返回两种场景
        syncPurchaseStatus()
//        throw Exception("test")
    }

    //检查用户的应用内购状态
    private fun checkPurchaseStatus(){
        mBillingLogic.init(this)
        // onCreate 时只做初始化，将状态同步移到 onResume
    }

    // 新增：唯一、权威的状态同步方法
    private fun syncPurchaseStatus() {
        lifecycleScope.launch {
            val isCurrentlyBought = mBillingLogic.isBoughtCheckReady(this@MainActivity)

            if (isCurrentlyBought) {
                // 如果用户当前已购买
                if (mPurchaseStatus != PurchaseStatus.BOUGHT) {
                    mPurchaseStatus = PurchaseStatus.BOUGHT
                    // UI 更新：移除广告和购买入口
                    destroyAdView()
                    if (this@MainActivity::mMenu.isInitialized) {
                        mMenu.findItem(R.id.removead)?.let { mMenu.removeItem(R.id.removead) }
                    }
                }
            } else {
                // 如果用户当前未购买
                if (mPurchaseStatus != PurchaseStatus.NO_BOUGHT) {
                    mPurchaseStatus = PurchaseStatus.NO_BOUGHT
                    // UI 更新：重新加载广告和购买入口
                    // 注意：initAd() 和菜单的逻辑需要确保能正确处理状态变化
                    initAd()
                    if (this@MainActivity::mMenu.isInitialized) {
                        if (mMenu.findItem(R.id.removead) == null) {
                            // 重新添加菜单项可能比较复杂，更简单的做法是调用 invalidateOptionsMenu()
                            // 它会触发 onCreateOptionsMenu() 重绘菜单
                            invalidateOptionsMenu()
                        }
                    }
                }
            }
        }
    }

    //加载广告
    private fun initAd(){
        if (mPurchaseStatus == PurchaseStatus.BOUGHT){
            return
        }

        /*adView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
                //adLoaded = false
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                //adLoaded = true
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }*/
        //初始化 AdMob
        if (App.adState != AdapterStatus.State.READY){
            MobileAds.initialize(this) {
                it.adapterStatusMap.get(MobileAds::class.qualifiedName)?.initializationState?.let {
                    App.adState = it
                    loadAd()
                    //adView.pause()
                }
            }
        } else {
            loadAd()
            //adView.pause()
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
            adView.visibility = View.GONE
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
                lifecycleScope.launch {
                    val isConnected = mBillingLogic.billingConnect(this@MainActivity)
                    if (isConnected) {
                        mBillingLogic.processPurchases(this@MainActivity)
                    } else {
                        // 可以选择性地弹出一个Toast提示连接失败
//                        ToastUtil.showToast(this@MainActivity, getString(R.string.billing_connect_failed))
                    }
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
                    try {
                        nm?.notify(0, it)
                    } catch (se: SecurityException) {
                        // POST_NOTIFICATIONS 权限被拒绝或通知被禁用
                    }
                }
            } else if (intent.action == "com.github.xiaofei_dev.vibrator.action" && !(mVibratorUtil?.isVibrate?:false)) {
                isInApp = true
                mVibratorUtil?.vibrate(mVibrateMode)
                textAction.setText(R.string.stop_vibrate)
                mAnimator?.start()
                setBottomBarVisibility()
                mRemoteViews?.setTextViewText(R.id.action, getString(R.string.remote_stop_vibrate))
                mNotification?.let {//更新通知
                    try {
                        nm?.notify(0, it)
                    } catch (se: SecurityException) {
                        // POST_NOTIFICATIONS 权限被拒绝或通知被禁用
                    }
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
        isKeep.isChecked = isChecked
        isKeep.setOnClickListener { v ->
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
                try {
                    nm?.notify(0, it)
                } catch (se: SecurityException) {
                    // POST_NOTIFICATIONS 权限被拒绝或通知被禁用
                }
            }
        }
        ////////////////发通知结束
        mAnimator = AnimatorInflater.loadAnimator(this@MainActivity, R.animator.anim_vibrate)
        mAnimator?.setTarget(textAction)

        /*btnExit.setOnClickListener {
            onBackPressedCallback.isEnabled = false
            onBackPressedDispatcher.onBackPressed()
        }

        btnBG.setOnClickListener {
            if (adLoaded) {
                adView.pause()
            }
            adShowing = false
            layoutAD.visibility = INVISIBLE
        }*/
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

        rootView.findViewById<View>(R.id.magenta).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.red).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.pink).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.yellow).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.green).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.blue).setOnClickListener(clickListener)
        rootView.findViewById<View>(R.id.black).setOnClickListener(clickListener)

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
        mNotification?.let {
            try {
                nm?.notify(0, it)
            } catch (se: SecurityException) {
                // POST_NOTIFICATIONS 权限被拒绝或通知被禁用
            }
        }
    }
}
