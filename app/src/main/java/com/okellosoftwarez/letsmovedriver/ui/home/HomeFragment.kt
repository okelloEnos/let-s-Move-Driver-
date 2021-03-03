package com.okellosoftwarez.letsmovedriver.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.okellosoftwarez.letsmovedriver.adapter.ordersAdapter
import com.okellosoftwarez.letsmovedriver.databinding.FragmentHomeBinding
import com.okellosoftwarez.letsmovedriver.model.receivedOrders
import com.okellosoftwarez.letsmovedriver.sharedViewModel.sharedViewModel

class HomeFragment : Fragment(){

    private lateinit var homeViewModel: sharedViewModel
    private lateinit var homeBinding: FragmentHomeBinding
    private lateinit var databaseInstance : FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var orderList: ArrayList<receivedOrders>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(requireActivity()).get(sharedViewModel::class.java)
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = homeBinding.root

        databaseInstance = FirebaseDatabase.getInstance()
        reference = databaseInstance.getReference("RelocationOrders")
        orderList = ArrayList()

        val layoutManager: LinearLayoutManager = LinearLayoutManager(requireContext())
        homeBinding.placedRecyclerView.setHasFixedSize(true)

        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (orderShot in snapshot.children) {
                    val order = orderShot.getValue<receivedOrders>()

                    if (order != null) {
                        order.id = orderShot.key
                        orderList.add(order)
                    }
                }
//                homeBinding.placedRecyclerView.layoutManager = layoutManager
                homeBinding.placedRecyclerView.adapter = ordersAdapter(orderList, homeViewModel)
                homeBinding.placedRecyclerView.layoutManager = layoutManager
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Listener", "onCancelled: " + error.message)
            }
        }

        reference.addValueEventListener(postListener)

        return root
    }
}