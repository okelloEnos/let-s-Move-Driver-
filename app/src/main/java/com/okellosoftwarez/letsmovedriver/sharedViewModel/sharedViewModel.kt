package com.okellosoftwarez.letsmovedriver.sharedViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.okellosoftwarez.letsmovedriver.model.receivedOrders

class sharedViewModel : ViewModel() {
//    fun sourceLocation(s: String?) {
//        sourceLocation.value = s
//    }

    fun clickedOrder(order: receivedOrders?) {
//        sourceLocation.value = s
        receivedOrd.value = order
    }

    var receivedOrd: MutableLiveData<receivedOrders> = MutableLiveData()
//    var sourceLocation: MutableLiveData<String> = MutableLiveData()
//    var destinationLocation: MutableLiveData<String> = MutableLiveData()
//    var sourceLongitude: MutableLiveData<Double> = MutableLiveData()
//    var sourceLatitude: MutableLiveData<Double> = MutableLiveData()
//    var destinationLongitude: MutableLiveData<Double> = MutableLiveData()
//    var destinationLatitude: MutableLiveData<Double> = MutableLiveData()
//    var sourceContact: MutableLiveData<String> = MutableLiveData()
//    var destinationContact: MutableLiveData<String> = MutableLiveData()
//    var scheduleTime: MutableLiveData<String> = MutableLiveData()
//    var scheduleDate: MutableLiveData<String> = MutableLiveData()
//    var vehicleType: MutableLiveData<String> = MutableLiveData()
//    var time: MutableLiveData<Double> = MutableLiveData()
//    var cost: MutableLiveData<Double> = MutableLiveData()



}