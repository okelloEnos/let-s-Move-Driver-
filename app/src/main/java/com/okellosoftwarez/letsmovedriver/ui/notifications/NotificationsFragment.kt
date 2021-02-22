package com.okellosoftwarez.letsmovedriver.ui.notifications

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.okellosoftwarez.letsmovedriver.R
import com.okellosoftwarez.letsmovedriver.util.GPSUtils

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private var isGPS : Boolean = false

    companion object {
        const val LOCATION_PERMISSION = 5
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProvider(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container , false)

//        GPSUtils(requireContext()).turnGPSOn(GPSUtils.onGpsListener) : onGpsL
        GPSUtils(requireContext()).turnGPSOn(object : GPSUtils.onGpsListener{
            override fun gpsStatus(isGPSEnable:Boolean) {
                // turn on GPS
                isGPS = isGPSEnable
            }
        })

        if (checkPermission()){
            Toast.makeText(requireContext(), "Permission on...", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(requireContext(), "Requesting for Permission...", Toast.LENGTH_LONG).show()
            requestPermissions(arrayOf <String> (Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION)
        }

        val textView: TextView = root.findViewById(R.id.text_notifications)
        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GPSUtils.GPS_REQUEST){
                isGPS = true
            }
        }
    }

    fun checkPermission() : Boolean{
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