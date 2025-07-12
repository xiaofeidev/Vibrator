package com.github.xiaofei_dev.vibrator.util

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine


/**
 * @author xiaofei_dev
 * @date 2023/5/3
 */
class BillingLogic(
    private val scope: CoroutineScope,
    boughtSuccess: () -> Unit
    ) {

    private val OK = BillingClient.BillingResponseCode.OK
    private val CANCELED = BillingClient.BillingResponseCode.USER_CANCELED

    private val productID = "2"

    private lateinit var billingClient: BillingClient

    @Volatile
    private var billingClientStatus = BillingClient.BillingResponseCode.SERVICE_DISCONNECTED

    //Google Play 内购商品回调
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            //onPurchasesUpdated
            // To be implemented in a later section.
            handleResult(billingResult.responseCode, okAction = {
                if (purchases != null){
                    for (purchase in purchases) {
                        scope.launch {
                            handlePurchase(purchase, boughtSuccess)
                        }
                    }
                }
            })
        }

    //初始化 BillingClient
    fun init(context: Context){
        billingClient = BillingClient.newBuilder(context.applicationContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    // 支持一次性商品挂起购买
                    .let { builder ->
                        builder.enableOneTimeProducts()
                        builder.build()
                    }
            )//支持待处理的交易
            .build()
    }

    //链接 GooglePlay 商品售卖服务 (重构为 suspend 函数)
suspend fun billingConnect(activity: Activity): Boolean {
        if (!this::billingClient.isInitialized) {
            init(activity)
        }

        // 如果已经连接，直接返回成功
        if (billingClient.isReady) {
            return true
        }

        // 使用 suspendCancellableCoroutine 包装回调
        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == OK) {
                        billingClientStatus = OK
                        if (continuation.isActive) {
                            continuation.resume(true, null)
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(false, null)
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    billingClientStatus = SERVICE_DISCONNECTED
                    // 服务断开，可以认为本次连接尝试失败
                    if (continuation.isActive) {
                        continuation.resume(false, null)
                    }
                }
            })
        }
    }

    suspend fun checkActiveBillingCheckReady(activity: Activity, boughtSuccess: () -> Unit) {
        val isConnected = billingConnect(activity)
        if (isConnected) {
            checkActiveBilling(boughtSuccess)
        }
    }

    //确保所有交易都得到确认，Activity 的 onResume 中调用
    private suspend fun checkActiveBilling(boughtSuccess: () -> Unit){
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)

        // uses queryPurchasesAsync Kotlin extension function
        val purchasesResult = billingClient.queryPurchasesAsync(params.build())
        if (purchasesResult.billingResult.responseCode == OK) {
            for (purchase in purchasesResult.purchasesList) {
                // 直接调用挂起函数，不再嵌套 scope.launch
                handlePurchase(purchase, boughtSuccess)
            }
        }
    }

    suspend fun isBoughtCheckReady(activity: Activity): Boolean {
        val isConnected = billingConnect(activity)
        if (!isConnected) {
            return false // 连接失败，无法检查购买状态
        }
        return isBought()
    }

    private suspend fun isBought(): Boolean {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)

        val purchasesResult = billingClient.queryPurchasesAsync(params.build())

        if (purchasesResult.billingResult.responseCode == OK) {
            val list = purchasesResult.purchasesList
            for (purchase in list) {
                //注意这种只在手机本地的验证方式无法得知退款等相关信息
                //如需得知商品订单退款等相关信息，则必须通过访问 Google Play Developer API
                //由于调用 Google Play Developer API 需要使用自己的访问令牌
                //而为了防止访问令牌泄露，我们最好从自己的服务端调用 Google Play Developer API
                //【注意】上面的的说法存疑，后来验证好像仅通过 play 结算系统便可过滤掉退款的情况
                if (purchase.products.getOrNull(0) == productID &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    purchase.isAcknowledged
//                    &&
//                    Security.isSignatureValid(purchase)
                ) {
                    // 验证购买交易
                    // 用户已经拥有该商品
                    return true
                }
            }
        }
        return false
    }

    //获取所有交易历史记录，该方法可用于在无自己的服务端的情况下检查用户商品购买状态
    //实践证明用不到此方法
    /*suspend fun checkHistory(){
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)

        // uses queryPurchaseHistory Kotlin extension function
        //返回用户针对每个商品发起的最近一笔购买记录
        val purchaseHistoryResult = billingClient.queryPurchaseHistory(params.build())
        handleResult(purchaseHistoryResult.billingResult.responseCode, okAction = {
            val recordList = purchaseHistoryResult.purchaseHistoryRecordList
            if (!recordList.isNullOrEmpty()){
                recordList.forEach {
                    it.products
                }
            } else {
                //查无记录，用户还未购买内购商品
            }
        })
    }*/

    //val acknowledgePurchaseResponseListener: AcknowledgePurchaseResponseListener = ...
    //确认购买交易的操作，一次性非消耗型商品
    private suspend fun handlePurchase(purchase: Purchase, boughtSuccess: () -> Unit) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // 如果购买已经确认，直接授予商品权益
            if (purchase.isAcknowledged) {
                boughtSuccess()
                return
            }

            // 如果购买尚未确认，则执行确认操作
            val acknowledgePurchaseParams =
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)

            val ackPurchaseResult = withContext(Dispatchers.IO) {
                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
            }
            handleResult(ackPurchaseResult.responseCode, okAction = {
                // 确认成功，授予商品权益
                boughtSuccess()
            })
        }
        // 可选：此处可处理 PENDING 状态的购买
    }

    //查询商品详情
    suspend fun processPurchases(activity: Activity) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        // leverage queryProductDetails Kotlin extension function
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }
        // Process the result.
        if (productDetailsResult.billingResult.responseCode == OK){
            val productDetails = productDetailsResult.productDetailsList?.getOrNull(0)
            productDetails?.let {
                launchOrder(activity, it)
            }
        }
    }

    //启动购买商品
    private fun launchOrder(activity: Activity, productDetails: ProductDetails){
        // 构建商品详情参数，仅订阅商品需要 offerToken
        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams
            .newBuilder()
            .setProductDetails(productDetails)

        productDetails.subscriptionOfferDetails?.getOrNull(0)?.offerToken?.let { token ->
            productDetailsParamsBuilder.setOfferToken(token)
        }

        val productDetailsParamsList = listOf(productDetailsParamsBuilder.build())

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setIsOfferPersonalized(false)//是否针对欧盟地区指明个性化价格
            .build()

        // Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        handleResult(billingResult.responseCode, okAction = {
            //启动购买界面成功
        })
    }

    //根据错误码判断是否可重试网络请求
    fun canRetry(errorCode: Int): Boolean {
        return (errorCode == SERVICE_TIMEOUT ||
            errorCode == SERVICE_DISCONNECTED ||
            errorCode == SERVICE_UNAVAILABLE ||
            errorCode == BILLING_UNAVAILABLE ||
            errorCode == ERROR ||
            errorCode == ITEM_ALREADY_OWNED ||
            errorCode == ITEM_NOT_OWNED ||
            errorCode == BillingClient.BillingResponseCode.NETWORK_ERROR)
    }

    //网络操作结果码判断操作逻辑
    private fun handleResult(resultCode: Int,
                     okAction: () -> Unit = {},
                     cancelAction: () -> Unit = {},
                     retryAction: () -> Unit = {},
                     giveUpAction: () -> Unit = {}
    ) {
        if (resultCode == OK) {
            okAction()
        } else if (resultCode == CANCELED) {
            cancelAction()
        } else if(canRetry(resultCode)){
            retryAction()
        } else {
            giveUpAction()
        }
    }
}
