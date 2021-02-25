package com.okellosoftwarez.letsmovedriver.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.okellosoftwarez.letsmovedriver.R
import com.okellosoftwarez.letsmovedriver.databinding.FragmentOrderDetailsBinding

/**
 * A simple [Fragment] subclass.
 * Use the [orderDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class orderDetailsFragment : Fragment() {
    private lateinit var detailsBinding: FragmentOrderDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        detailsBinding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        val view = detailsBinding.root



        return view
    }

}