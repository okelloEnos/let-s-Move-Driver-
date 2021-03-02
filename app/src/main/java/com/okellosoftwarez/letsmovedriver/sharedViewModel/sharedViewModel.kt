package com.okellosoftwarez.letsmovedriver.sharedViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.okellosoftwarez.letsmovedriver.model.receivedOrders

class sharedViewModel : ViewModel() {

    fun clickedOrder(order: receivedOrders?) {
        receivedOrd.value = order
    }

    fun keepTrack(map: MapboxMap){
        mapBoxMap.value = map
    }
    var receivedOrd: MutableLiveData<receivedOrders> = MutableLiveData()
    var mapBoxMap : MutableLiveData<MapboxMap> = MutableLiveData()

}