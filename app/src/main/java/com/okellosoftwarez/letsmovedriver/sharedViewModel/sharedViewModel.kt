package com.okellosoftwarez.letsmovedriver.sharedViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.okellosoftwarez.letsmovedriver.model.receivedOrders

class sharedViewModel : ViewModel() {

    fun clickedOrder(order: receivedOrders?) {
        receivedOrd.value = order
    }

    var receivedOrd: MutableLiveData<receivedOrders> = MutableLiveData()
}