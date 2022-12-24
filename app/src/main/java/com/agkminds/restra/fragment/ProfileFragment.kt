package com.agkminds.restra.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Binder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.agkminds.restra.R
import com.agkminds.restra.databinding.FragmentProfileBinding
import com.agkminds.restra.util.SessionManager

class ProfileFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var sessionManager: SessionManager
    lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment


        sessionManager = SessionManager(activity as Context)
        sharedPreferences = (activity as Context).getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)


        binding.txtUserName.text = sharedPreferences.getString("user_name", null)
        binding.txtPhoneNumber.text =
            sharedPreferences.getString("user_mobile_number", null)
        binding.txtEmailAddress.text = sharedPreferences.getString("user_email", null)
        binding.txtDeliveryAddress.text =
            sharedPreferences.getString("user_address", null)

        return binding.root
    }
}