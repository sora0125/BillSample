package jp.ne.sonet.blog.sora0125.billsample

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.android.billingclient.api.*
import io.reactivex.Single


object BillingManager {
    lateinit private var billingClient: BillingClient
    private var detailsList = mutableListOf<SkuDetails>()
    private val TAG = javaClass.simpleName
    private val listener = PurchasesUpdatedListener { responseCode, purchases ->
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (purchase in purchases) {
                Log.d(TAG, "purchase:$purchase")
                if (purchase.sku == "android.test.purchased") {
                    consumeAsync(purchase.purchaseToken)
                }
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    fun startConnection(context: Context, itemList: ArrayList<String>): Single<MutableList<SkuDetails>> {
        return  Single.create<MutableList<SkuDetails>> {
            // Google Playとの接続クライアント初期化
            billingClient = BillingClient.newBuilder(context).setListener(listener).build()
                    billingClient.startConnection(object : BillingClientStateListener {
                        // 接続終了コールバック
                        override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                            // 接続成功
                            if (billingResponseCode == BillingClient.BillingResponse.OK) {
                                // The billing client is ready. You can query purchases here.
                                val skuList = ArrayList<String>()
                                // アイテムIDセット
                                skuList.addAll(itemList)
                                val params = SkuDetailsParams.newBuilder()
                                // 消費型アイテムセット
                                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                                // 商品の詳細情報を取得
                                billingClient.querySkuDetailsAsync(params.build()) { responseCode, skuDetailsList ->
                                    // Process the result.
                                    // 取得成功
                                    if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                                        detailsList = skuDetailsList
                                        Log.d(TAG, "skuDetailList:$skuDetailsList")
                                        Log.d(TAG, "detailsList:$detailsList")
                                        it.onSuccess(detailsList)
                                    }
                                }
                            }
                        }

                        override fun onBillingServiceDisconnected() {
                            // Try to restart the connection on the next request to
                            // Google Play by calling the startConnection() method.

                        }
                    })
        }
    }

    fun startPurchasesFrow(itemList: SkuDetails, activity: FragmentActivity) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(itemList)
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun queryPurchases(context: Context) {
//        billingClient = BillingClient.newBuilder(context).setListener(listener).build()
        val purchasesResult: Purchase.PurchasesResult =
            billingClient.queryPurchases(BillingClient.SkuType.INAPP)
        Log.d(TAG, "purchasesResult.responseCode:${purchasesResult.responseCode}")

        if (purchasesResult.responseCode == BillingClient.BillingResponse.OK) {
            for (skudetail in purchasesResult.purchasesList) {
                Log.d(TAG, "skudetail:$skudetail")
            }
        }else {
        }
    }

    fun consumeAsync(purchaseToken: String) {
        billingClient.consumeAsync(purchaseToken) { responseCode, outToken ->
            if (responseCode == BillingClient.BillingResponse.OK) {
                // Handle the success of the consume operation.
                // For example, increase the number of coins inside the user's basket.
                Log.d(TAG, "responseCode:$responseCode")
                Log.d(TAG, "outToken:$outToken")
            }
        }
    }
}