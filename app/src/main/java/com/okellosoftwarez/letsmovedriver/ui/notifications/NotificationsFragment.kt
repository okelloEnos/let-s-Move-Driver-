package com.okellosoftwarez.letsmovedriver.ui.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.okellosoftwarez.letsmovedriver.R
import com.okellosoftwarez.letsmovedriver.databinding.FragmentNotificationsBinding
import com.okellosoftwarez.letsmovedriver.util.GPSUtils

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var trackMapBoxMap: MapboxMap
    private var isGPS : Boolean = false

    companion object {
        const val LOCATION_PERMISSION = 5
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        Mapbox.getInstance(requireContext(), getString(R.string.public_token))

        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root : View = binding.root

        notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

        GPSUtils(requireContext()).turnGPSOn(object : GPSUtils.onGpsListener{
            override fun gpsStatus(isGPSEnable:Boolean) {
                // turn on GPS
                isGPS = isGPSEnable
            }
        })

//        if (checkPermission()){
//            Toast.makeText(requireContext(), "Permission on...", Toast.LENGTH_SHORT).show()
//        }
//        else{
//            Toast.makeText(requireContext(), "Requesting for Permission...", Toast.LENGTH_LONG).show()
//            requestPermissions(arrayOf <String> (Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
//        }

//        val textView: TextView = root.findViewById(R.id.text_notifications)
//        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        binding.notificationMapView.onCreate(savedInstanceState)
        binding.notificationMapView.getMapAsync(object : OnMapReadyCallback{
            override fun onMapReady(mapboxMap: MapboxMap){
                trackMapBoxMap = mapboxMap
                trackMapBoxMap.setStyle(Style.MAPBOX_STREETS, object : Style.OnStyleLoaded{

                    override fun onStyleLoaded(style : Style){
                        if (checkPermission()){

                            Toast.makeText(requireContext(), "Map has permission already", Toast.LENGTH_SHORT).show()
                            enablePersonalLocation(style)
                        }
                        else{
                            Toast.makeText(requireContext(), "Map requesting Permission", Toast.LENGTH_SHORT).show()
                            requestPermissions(arrayOf<String> (Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
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
                if (grantResults?.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(requireContext(), "Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}