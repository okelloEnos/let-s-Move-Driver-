package com.okellosoftwarez.letsmovedriver.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.okellosoftwarez.letsmovedriver.R
import com.okellosoftwarez.letsmovedriver.databinding.FragmentDashboardBinding
import com.okellosoftwarez.letsmovedriver.sharedViewModel.sharedViewModel

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: sharedViewModel
    private lateinit var dashboardBinding: FragmentDashboardBinding
    private lateinit var DashBoardReference: DatabaseReference

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel = ViewModelProvider(requireActivity()).get(sharedViewModel::class.java)
        dashboardBinding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root = dashboardBinding.root

        DashBoardReference = FirebaseDatabase.getInstance().getReference("TrackingNode")
        dashboardViewModel.receivedOrd.observe(viewLifecycleOwner, Observer {
            dashboardBinding.dynamicLocationSourceDashboard.text = it.sourceLocation
            dashboardBinding.dynamicDestinationSourceDashboard.text = it.destinationLocation
            dashboardBinding.dynamicVehicleDashboard.text = it.vehicleType
            dashboardBinding.dynamicDateDashboard.text = it.scheduleDate
            dashboardBinding.dynamicTimeDashboard.text = it.scheduleTime
            dashboardBinding.dynamicCostDashboard.text = it.cost.toString()
            dashboardBinding.dynamicSourceContact.text = it.sourceContact
            dashboardBinding.dynamicDestinationContact.text = it.destinationContact
        })

        dashboardBinding.acceptRequest.setOnClickListener(View.OnClickListener {

            dashboardViewModel.receivedOrd.observe(viewLifecycleOwner, Observer {
                val orderKey : String? = it.id
                DashBoardReference.child(orderKey!!).child("DriverResponse").setValue("YES")
            })
        })
        return root
    }
}