package com.okellosoftwarez.letsmovedriver.util

import android.location.Location
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.okellosoftwarez.letsmovedriver.ui.notifications.NotificationsFragment
import java.lang.ref.WeakReference

class locationUpdater(var fragment: NotificationsFragment) : LocationEngineCallback<LocationEngineResult> {

    companion object {
        var  location : Location? = null
    }

//    var  location : Location? = null
    private val weakFragmentReference : WeakReference<NotificationsFragment> = WeakReference(fragment)

    /**
     * The LocationEngineCallback interface's method which fires when the device's location has changed.
     *
     * @param result the LocationEngineResult object which has the last known location within it.
     */

    override fun onSuccess(result: LocationEngineResult?) {

        if (weakFragmentReference.get() == null || result == null || result.lastLocation == null) {
            return
        }
        else {
             location = result.lastLocation
        }

    }

    /**
     * The LocationEngineCallback interface's method which fires when the device's location can not be captured
     *
     * @param exception the exception message
     */
    override fun onFailure(exception: Exception) {
        if (weakFragmentReference.get() == null)
            return
//        Toast.makeText(weakFragmentReference, "Error : " + exception.localizedMessage, Toast.LENGTH_SHORT).show()
    }
}