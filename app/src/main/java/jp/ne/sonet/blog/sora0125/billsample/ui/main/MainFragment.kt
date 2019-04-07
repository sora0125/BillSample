package jp.ne.sonet.blog.sora0125.billsample.ui.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.android.billingclient.api.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.ne.sonet.blog.sora0125.billsample.BillingManager
import jp.ne.sonet.blog.sora0125.billsample.R
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {
//    , PurchasesUpdatedListener {
    private var buttonList = mutableListOf<Button>()
    private var skuDeailList = mutableListOf<SkuDetails>()
    private val TAG = javaClass.simpleName

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel


        context?.let { context ->

            // コネクションスタート
            val skuList = ArrayList<String>()
            // アイテムIDセット
            skuList.add("android.test.purchased")
            skuList.add("android.test.canceled")
            skuList.add("android.test.refunded")
            skuList.add("android.test.item_unavailable")
            // コネクションスタート
            BillingManager.startConnection(context, skuList)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {skuDetailsList ->
                    Log.d(TAG, "skuDetailList:$skuDetailsList")
                    for ((index, skuDetails) in skuDetailsList.withIndex()) {
                        buttonList.add(Button(context))
                        buttonList[index].text = skuDetails.sku
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                        buttonList[index].layoutParams = layoutParams

                        main_layout.addView(buttonList[index])

                        buttonList[index].setOnClickListener {
                            activity?.let {act ->
                                BillingManager.startPurchasesFrow(skuDetails, act)
                            }
                        }
                    }
                }
            BillingManager.queryPurchases(context)
        }

    }
}
