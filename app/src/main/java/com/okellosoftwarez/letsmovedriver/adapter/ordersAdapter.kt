package com.okellosoftwarez.letsmovedriver.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.okellosoftwarez.letsmovedriver.R
import com.okellosoftwarez.letsmovedriver.databinding.OrdersRecyclerBinding
import com.okellosoftwarez.letsmovedriver.model.receivedOrders
import com.okellosoftwarez.letsmovedriver.sharedViewModel.sharedViewModel

class ordersAdapter (private val orderList: ArrayList<receivedOrders>,
private val viewModel: sharedViewModel) : RecyclerView.Adapter<ordersAdapter.ordersViewHolder>(){

    class ordersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val locationSource : TextView = itemView.findViewById(R.id.dynamicLocationSource)
        val locationDestination : TextView = itemView.findViewById(R.id.dynamicDestinationSource)
        val vehicleType : TextView = itemView.findViewById(R.id.dynamicVehicle)
        val costView : TextView = itemView.findViewById(R.id.dynamicCost)
        val orderView : CardView = itemView.findViewById(R.id.ordersCardView)

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ordersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.orders_recycler, parent, false)
        return ordersViewHolder(view)
    }

    override fun onBindViewHolder(holder: ordersViewHolder, position: Int) {
        val currentOrder = orderList[position]
        holder.locationSource.text = currentOrder.sourceLocation
        holder.locationDestination.text = currentOrder.destinationLocation
        holder.vehicleType.text = currentOrder.vehicleType
        holder.costView.text = currentOrder.cost.toString()
        holder.orderView.setOnClickListener{
            Log.d("Click", "onBindViewHolder: $position")
            viewModel.clickedOrder(currentOrder)
        }
    }

    override fun getItemCount(): Int {
        if (orderList != null){
            return orderList.size
        }
        return 0
    }
}