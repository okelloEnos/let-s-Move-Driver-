package com.okellosoftwarez.letsmovedriver.model

data class receivedOrders(val sourceLocation: String? = null, val destinationLocation: String? = null,
                          val sourceLocationLatitude: Double? = null, val destinationLocationLatitude: Double? = null,
                          val sourceLocationLongitude: Double? = null, val destinationLocationLongitude: Double? = null,
                          val sourceContact: String? = null, val destinationContact: String? = null,
                          val scheduleTime: String? = null, val scheduleDate: String? = null,
                          val vehicleType: String? = null, val time: Double? = null,
                          val cost: Double? = null){


}
