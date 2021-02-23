package com.okellosoftwarez.letsmovedriver.ui.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mapbox.android.core.location.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.okellosoftwarez.letsmovedriver.R
import com.okellosoftwarez.letsmovedriver.databinding.FragmentNotificationsBinding
import com.okellosoftwarez.letsmovedriver.util.GPSUtils
import com.okellosoftwarez.letsmovedriver.util.locationUpdater


class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private var binding: FragmentNotificationsBinding? = null
    private lateinit var trackMapBoxMap: MapboxMap
    private var isGPS : Boolean = false
    // Variables needed to add the location engine
    private lateinit var locationEngine: LocationEngine
    private val DEFAULT_INTERVAL_IN_MILLISECONDS : Long = 1000L
    private val DEFAULT_MAX_WAIT_TIME : Long = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    val databaseInstance = Firebase.database
    val myRef = databaseInstance.getReference("Message")


    // Variables needed to listen to location updates
    private val callback : locationUpdater = locationUpdater(this)

    companion object {
        const val LOCATION_PERMISSION = 5
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        Mapbox.getInstance(requireContext(), getString(R.string.public_token))

        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root : View = binding!!.root

        myRef.setValue("Evette || Bijuma")

        notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

        GPSUtils(requireContext()).turnGPSOn(object : GPSUtils.onGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                // turn on GPS
                isGPS = isGPSEnable
            }
        })

        binding?.notificationMapView?.onCreate(savedInstanceState)
        binding?.notificationMapView?.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(mapboxMap: MapboxMap) {
                trackMapBoxMap = mapboxMap
                trackMapBoxMap.setStyle(Style.MAPBOX_STREETS, object : Style.OnStyleLoaded {

                    override fun onStyleLoaded(style: Style) {
                        if (checkPermission()) {

                            Toast.makeText(requireContext(), "Map has permission already", Toast.LENGTH_SHORT).show()

                            enablePersonalLocation(style)

                        } else {
                            Toast.makeText(requireContext(), "Map requesting Permission", Toast.LENGTH_SHORT).show()
                            requestPermissions(arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
                        }

                        // Add the symbol layer icon to map for future use
                        // Add the red marker icon image to the map
//                        style.addImage(symbolIconId, BitmapUtils.getBitmapFromDrawable(
//                            getResources().getDrawable(R.drawable.marker_location)));

                        // Create an empty GeoJSON source using the empty feature collection
//                        setUpSource(style);
//
//                        setUpDestination(style);
//
//                        setRouteLayer(style);

                    }
                })

            }
        })

        return root
    }

    @SuppressLint("MissingPermission")
    private fun enablePersonalLocation(style: Style) {

        val locationComponentOptions : LocationComponentOptions = LocationComponentOptions.builder(requireContext())
            .pulseEnabled(true)
            .pulseColor(Color.parseColor("#86C0ED"))
            .foregroundTintColor(Color.BLUE)
            .pulseAlpha(.4f)
            .pulseInterpolator(BounceInterpolator())
            .build()

        val locationComponentActivationOptions : LocationComponentActivationOptions = LocationComponentActivationOptions
                .builder(requireContext(), style)
                .locationComponentOptions(locationComponentOptions)
                .build()

//        get instance of location component
        val locationComponent = trackMapBoxMap.locationComponent

//        activate location component with options
        locationComponent.activateLocationComponent(locationComponentActivationOptions)

//        enable to make the location component visible
        locationComponent.isLocationComponentEnabled = true

//        set the location component camera mode
        locationComponent.cameraMode = CameraMode.TRACKING

//        set render mode
        locationComponent.renderMode = RenderMode.COMPASS

//        var locality : Location? = plocationComponent.lastKnownLocation
//        Log.d("Locality", "enablePersonalLocation: Lat : " + locality?.latitude + " Long : " + locality?.longitude)

        initLocationEngine()

    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        val request : LocationEngineRequest = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build()
//        val locality : Location = locationEngine.getLastLocation()
//        var locationEng : LocationEngine = LocationEngineProvider(requireActivity()).obtainBestLocationEngineAvailable();
//        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
//        locationEngine.activate();
        locationEngine.requestLocationUpdates(request, callback, Looper.getMainLooper())

        locationEngine.getLastLocation(callback)

//        Toast.makeText(requireContext(), "Change to : Lat : " + locationUpdater.location?.latitude + " Long : " + locationUpdater.location?.longitude , Toast.LENGTH_LONG).show()
        Log.d("Frag", "initLocationEngine: Lat : " + locationUpdater.location?.latitude + " Long : " + locationUpdater.location?.longitude)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GPSUtils.GPS_REQUEST){
                isGPS = true
            }
        }
    }

    private fun checkPermission() : Boolean{
        return ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            LOCATION_PERMISSION -> {
//                if the request is cancelled arrays are always empty
                if (grantResults?.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
//                    enablePersonalLocation(trackMapBoxMap.style!!)
                    trackMapBoxMap.getStyle() {
                        object : Style.OnStyleLoaded {
                            override fun onStyleLoaded(style: Style) {
                                enablePersonalLocation(style)
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        binding?.notificationMapView?.onStart()
//        if (locationEngine != null) locationEngine.requestLocationUpdates()
        Log.d("FragStart", "initLocationEngine: Lat : " + locationUpdater.location?.latitude + " Long : " + locationUpdater.location?.longitude)

    }

    override fun onResume() {
        super.onResume()
        Log.d("FragResume", "initLocationEngine: Lat : " + locationUpdater.location?.latitude + " Long : " + locationUpdater.location?.longitude)

        Toast.makeText(requireContext(), "Change to : Lat : " + locationUpdater.location?.latitude + " Long : " + locationUpdater.location?.longitude, Toast.LENGTH_LONG).show()
        binding?.notificationMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding?.notificationMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding?.notificationMapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding?.notificationMapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding?.notificationMapView?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (locationEngine != null){
            locationEngine.removeLocationUpdates(callback)
        }

        binding?.notificationMapView?.onDestroy()
        binding = null

    }

//  private class location_Updater(fragment: NotificationsFragment) : LocationEngineCallback<LocationEngineResult> {
//
////        private var  location : Location? = null
//        private val weakFragmentReference : WeakReference<NotificationsFragment> = WeakReference(fragment)
//
//        /**
//         * The LocationEngineCallback interface's method which fires when the device's location has changed.
//         *
//         * @param result the LocationEngineResult object which has the last known location within it.
//         */
//
//        override fun onSuccess(result: LocationEngineResult?) {
//
//            if (weakFragmentReference.get() == null || result == null || result.lastLocation == null) {
//                return
//            }
//            else {
//                weakFragmentReference.get()!!.trackMapBoxMap.locationComponent.forceLocationUpdate(result.lastLocation)
//                location = result.lastLocation
//
//            }
//
//        }
//
//        /**
//         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
//         *
//         * @param exception the exception message
//         */
//        override fun onFailure(exception: Exception) {
//            if (weakFragmentReference.get() == null)
//                return
////        Toast.makeText(weakFragmentReference, "Error : " + exception.localizedMessage, Toast.LENGTH_SHORT).show()
//        }
//    }
}