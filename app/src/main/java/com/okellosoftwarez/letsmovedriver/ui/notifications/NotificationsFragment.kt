package com.okellosoftwarez.letsmovedriver.ui.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.okellosoftwarez.letsmovedriver.R
import com.okellosoftwarez.letsmovedriver.adapter.ordersAdapter
import com.okellosoftwarez.letsmovedriver.databinding.FragmentNotificationsBinding
import com.okellosoftwarez.letsmovedriver.model.receivedOrders
import com.okellosoftwarez.letsmovedriver.sharedViewModel.sharedViewModel
import com.okellosoftwarez.letsmovedriver.util.GPSUtils
import com.okellosoftwarez.letsmovedriver.util.locationUpdater
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates


class NotificationsFragment : Fragment() {

    private lateinit var NotificationReference: DatabaseReference
    private val TAG = "NotificationsFragment"
    private lateinit var notificationsViewModel: sharedViewModel
    private var binding: FragmentNotificationsBinding? = null
    private var trackMapBoxMap: MapboxMap? = null
    private var isGPS: Boolean = false

    // Variables needed to add the location engine
    private lateinit var locationEngine: LocationEngine
    private val DEFAULT_INTERVAL_IN_MILLISECONDS: Long = 1000L
    private val DEFAULT_MAX_WAIT_TIME: Long = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    private var sourceLat: Double? = null
    private var sourceLong: Double? = null
    private var destinationLat: Double = 0.0
    private var destinationLong: Double? = null
    private val symbolIconId = "symbolIconId"
    private val geoSourceLayerId = "sourceLayerId"
    private val geoDestinationId = "destLayerId"
    private val ROUTE_LAYER_ID = "route-layer-id"
    private val ROUTE_SOURCE_ID = "route-source-id"
    private lateinit var client: MapboxDirections
    private lateinit var currentRoute: DirectionsRoute
    private var driverLat: Double? = null
    private var driverLong: Double? = null

//    private val databaseInstance = Firebase.database
//    val myRef = databaseInstance.getReference("mapMap")
//    private val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(Shared_File, Context.MODE_PRIVATE)


    // Variables needed to listen to location updates
    private val callback: locationUpdater = locationUpdater(this)

    companion object {
        const val LOCATION_PERMISSION = 5
        const val Shared_File = "MapBoxMapFile"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        Log.d(TAG, "onCreateView: " + "1")
        Mapbox.getInstance(requireContext(), getString(R.string.public_token))

        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        NotificationReference = FirebaseDatabase.getInstance().getReference("TrackingNode")
        notificationsViewModel = ViewModelProvider(requireActivity()).get(sharedViewModel::class.java)

        notificationsViewModel.receivedOrd.observe(viewLifecycleOwner, Observer {
            sourceLat = it.sourceLocationLatitude
        })
        notificationsViewModel.receivedOrd.observe(viewLifecycleOwner, Observer {
            sourceLong = it.sourceLocationLongitude
        })
        notificationsViewModel.receivedOrd.observe(viewLifecycleOwner, Observer {
            destinationLat = it.destinationLocationLatitude!!
        })
        notificationsViewModel.receivedOrd.observe(viewLifecycleOwner, Observer {
            destinationLong = it.destinationLocationLongitude
        })

        GPSUtils(requireContext()).turnGPSOn(object : GPSUtils.onGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                // turn on GPS
                isGPS = isGPSEnable
            }
        })

        binding?.reachSourceBtn?.setOnClickListener(View.OnClickListener {
            notificationsViewModel.receivedOrd.observe(viewLifecycleOwner, Observer { it ->
                val changedLat: Double? = it.destinationLocationLatitude
                val changedLong: Double? = it.destinationLocationLongitude
//                sourceLat = soLoc
//                sourceLong = it.destinationLocationLongitude
//                destinationLat = it.destinationLocationLatitude!!
                val orderKey : String? = it.id
                NotificationReference.child(orderKey!!).child("SourceResponse").setValue("YES")

                if (trackMapBoxMap == null){
                    notificationsViewModel.mapBoxMap.observe(viewLifecycleOwner, Observer {
                        trackMapBoxMap = it
                    })
                }

                driverLat = locationUpdater.location?.latitude
                driverLong = locationUpdater.location?.longitude

                getRoutes(driverLat!!, driverLong!!, changedLat!!, changedLong!! )

                it.sourceLocationLatitude = changedLat
                it.sourceLocationLongitude = changedLong
//                notificationsViewModel.receivedOrd.observe(viewLifecycleOwner, Observer {
//                    sourceLat = it.destinationLocationLatitude
//                })
//                notificationsViewModel.receivedOrd.observe(viewLifecycleOwner, Observer {
//                    sourceLong = it.destinationLocationLongitude
//                })
//                sourceLat = it.destinationLocationLatitude
//                sourceLong = it.destinationLocationLongitude
//                binding?.reachSourceBtn?.text = "Heading to Destination"
                binding?.headingDestBtn?.visibility = View.VISIBLE
                binding?.reachSourceBtn?.visibility = View.INVISIBLE
                binding?.headingDestBtn?.setOnClickListener {
                    NotificationReference.child(orderKey!!).child("DestinationResponse").setValue("YES")
//                    binding?.reachSourceBtn?.text = "Delivery Complete"
                    binding?.deliveryDestBtn?.visibility = View.VISIBLE
                    binding?.headingDestBtn?.visibility = View.INVISIBLE
                    binding?.deliveryDestBtn?.setOnClickListener {
                        NotificationReference.child(orderKey!!).child("DeliveryResponse").setValue("COMPLETE")
                    }
                }
            })
        })

        binding?.notificationMapView?.onCreate(savedInstanceState)

        binding?.notificationMapView?.getMapAsync { mapboxMap ->

            trackMapBoxMap = mapboxMap

            notificationsViewModel.keepTrack(mapboxMap)

            trackMapBoxMap?.setStyle(Style.MAPBOX_STREETS) { style ->
                if (checkPermission()) {

                    Toast.makeText(requireContext(), "Map has permission already", Toast.LENGTH_SHORT).show()

                    enablePersonalLocation(style)

                } else {
                    Toast.makeText(requireContext(), "Map requesting Permission", Toast.LENGTH_SHORT).show()
                    requestPermissions(arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
                }

                style.addImage(symbolIconId, BitmapUtils.getBitmapFromDrawable(
                        resources.getDrawable(R.drawable.marker_location, null))!!)

                //                         Create an empty GeoJSON source using the empty feature collection
                setUpSource(style);
                //
                setUpDestination(style);
                //
                setRouteLayer(style);
            }
        }

        return root
    }

    private fun setRouteLayer(style: Style) {
//        add routes sources to the map
        style.addSource(GeoJsonSource(ROUTE_SOURCE_ID))

//        add route layer to the map
        val routeLayer: LineLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#009688"))
        )
        style.addLayer(routeLayer)
    }

    private fun setUpDestination(style: Style) {
        if (sourceLat == null || sourceLong == null) {
            style.addSource(GeoJsonSource(geoDestinationId))
        } else {
//            add marker destination coordinates to the map
            style.addSource(GeoJsonSource(geoDestinationId, FeatureCollection.fromFeatures(arrayOf<Feature>(
                    Feature.fromGeometry(Point.fromLngLat(sourceLong!!, sourceLat!!))
            ))))

            if (driverLong != null && driverLat != null) {
                bound()
            }

        }

//        add the marker icon symbol layer to map
        style.addLayer(SymbolLayer("layer_destination", geoDestinationId)
                .withProperties(iconImage(symbolIconId), iconOffset(arrayOf<Float>(0f, -8f))))
    }

    private fun setUpSource(style: Style) {
        if (driverLat == null || driverLong == null) {
            style.addSource(GeoJsonSource(geoSourceLayerId))
        } else {

//            add driver marker
            style.addSource(GeoJsonSource(geoSourceLayerId,
                    FeatureCollection.fromFeatures(arrayOf<Feature>(
                            Feature.fromGeometry(Point.fromLngLat(driverLong!!, driverLat!!))
                    ))))

            if (sourceLat != null && sourceLong != null) {
                bound()
            }
        }
//        add the marker to the map
        style.addLayer(SymbolLayer("layer_id", geoSourceLayerId)
                .withProperties(iconImage(symbolIconId), iconOffset(arrayOf<Float>(0f, -8f))))
    }

    private fun bound() {
        val latLngBounds: LatLngBounds = LatLngBounds.Builder()
                .include(LatLng(driverLat!!, driverLong!!))
                .include(LatLng(sourceLat!!, sourceLong!!))
                .build()

        trackMapBoxMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10))
    }

    @SuppressLint("MissingPermission")
    private fun enablePersonalLocation(style: Style) {

        val locationComponentOptions: LocationComponentOptions = LocationComponentOptions.builder(requireContext())
                .pulseEnabled(true)
                .pulseColor(Color.parseColor("#86C0ED"))
                .foregroundTintColor(Color.BLUE)
                .pulseAlpha(.4f)
                .pulseInterpolator(BounceInterpolator())
                .build()

        val locationComponentActivationOptions: LocationComponentActivationOptions = LocationComponentActivationOptions
                .builder(requireContext(), style)
                .locationComponentOptions(locationComponentOptions)
                .build()

//        get instance of location component
        val locationComponent = trackMapBoxMap?.locationComponent

//        activate location component with options
        locationComponent?.activateLocationComponent(locationComponentActivationOptions)

//        enable to make the location component visible
        locationComponent?.isLocationComponentEnabled = true

//        set the location component camera mode
        locationComponent?.cameraMode = CameraMode.TRACKING

//        set render mode
        locationComponent?.renderMode = RenderMode.COMPASS

        initLocationEngine()

    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        val request: LocationEngineRequest = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build()

        locationEngine.requestLocationUpdates(request, callback, Looper.getMainLooper())

        locationEngine.getLastLocation(callback)
        driverLat = locationUpdater.location?.latitude
        driverLong = locationUpdater.location?.longitude

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPSUtils.GPS_REQUEST) {
                isGPS = true
            }
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION -> {
//                if the request is cancelled arrays are always empty
                if (grantResults?.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
                    trackMapBoxMap?.getStyle() {
                        Style.OnStyleLoaded { style -> enablePersonalLocation(style) }
                    }
                } else {
                    Toast.makeText(requireContext(), "Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getRoutes(driverLat: Double, driverLong: Double, sourceLat: Double, sourceLong: Double) {

        client = MapboxDirections.builder()
                .origin(Point.fromLngLat(driverLong, driverLat))
                .destination(Point.fromLngLat(sourceLong, sourceLat))
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.public_token))
                .build()

        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response == null) {
                    Toast.makeText(requireContext(), "No routes found, make sure you set the right user and access token.", Toast.LENGTH_SHORT).show()
                    return
                } else if (response.body()?.routes()?.size!! < 1) {
                    Toast.makeText(requireContext(), "No routes found", Toast.LENGTH_SHORT).show()
                    return
                }
//                get directions route
                currentRoute = response!!.body()!!.routes()[0]

                if (trackMapBoxMap != null) {
                    trackMapBoxMap?.getStyle { style ->
                        //                            Retrieve and update the layer responsible for showing directions
                        val source: GeoJsonSource? = style.getSourceAs(ROUTE_SOURCE_ID)

                        //              Create a LineString with the directions route's geometry and
                        //              reset the GeoJSON source for the route LineLayer source
                        source?.setGeoJson(LineString.fromPolyline(currentRoute.geometry()!!, PRECISION_6))
                    }
                }

//                val distanceRem: Double = currentRoute.distance()
//                if(distanceRem <= 0){
//                    binding?.reachSourceBtn?.visibility = View.VISIBLE
//                }

            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error : " + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        binding?.notificationMapView?.onStart()

    }
    override fun onResume() {
        notificationsViewModel.mapBoxMap.observe(viewLifecycleOwner, Observer {
            trackMapBoxMap = it
        })

//        binding?.reachSourceBtn?.visibility = View.INVISIBLE

        driverLat = locationUpdater.location?.latitude
        driverLong = locationUpdater.location?.longitude

        if (trackMapBoxMap != null) {
            if (driverLat != null && driverLong != null && sourceLong != null && sourceLat != null) {
                getRoutes(driverLat!!, driverLong!!, sourceLat!!, sourceLong!!)
            }
        }
        super.onResume()
        binding?.notificationMapView?.onResume()
    }

    override fun onStop() {
        super.onStop()
        binding?.notificationMapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding?.notificationMapView?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding?.notificationMapView?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback)
        }
        binding?.notificationMapView?.onDestroy()
        binding = null

    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding?.notificationMapView?.onLowMemory()
    }
}
/**
 * Shifting direction and maps towards destination after arriving at destination
 */