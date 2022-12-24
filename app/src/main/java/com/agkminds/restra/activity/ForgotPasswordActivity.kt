package com.agkminds.restra.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.agkminds.restra.R
import com.agkminds.restra.databinding.ActivityForgotPasswordBinding
import com.agkminds.restra.util.*
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("preference_name", MODE_PRIVATE)


        //        Getting Credentials
        val phoneNumber = binding.etPhoneNumber.toString()
        val emailAddress = binding.etEmailAddress.toString()

        binding.btnNext.setOnClickListener {
            if (phoneNumber != "" && emailAddress != "") {

                if (ConnectionManager().isNetworkAvailable(this)) {

//        Using Volley-Library to fetch JSON objects from API
                    val queue = Volley.newRequestQueue(this)

                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number", phoneNumber)
                    jsonParams.put("email", emailAddress)

//        Progress bar displaying
                    binding.progressBar.visibility = View.VISIBLE

                    val jsonObjectRequest =
                        object :
                            JsonObjectRequest(
                                Request.Method.POST,
                                FORGOT_PASSWORD,
                                jsonParams,
                                Response.Listener<JSONObject> { it ->
                                    println(it.toString())
                                    try {
                                        binding.progressBar.visibility =
                                            View.GONE // This will hide the progress bar when the content is loaded.

                                        var data = it.getJSONObject("data")
                                        var success = data.getBoolean("success")
                                        if (success) {
                                            val firstTry = data.getBoolean("first_try")

                                            if (firstTry) {
                                                binding.progressBar.visibility = View.GONE
                                                binding.recoveryLayout.visibility = View.GONE
                                                binding.otpLayout.visibility = View.VISIBLE

                                                val otp = binding.etOTP.toString()
                                                val newPassword = binding.etNewPassword.toString()
                                                val newConfirmPassword =
                                                    binding.etConfirmNewPassword.toString()

                                                binding.btnSubmit.setOnClickListener {
                                                    if (otp != "" && newPassword != "" && newConfirmPassword != "") {

                                                        if (newPassword != newConfirmPassword) {
                                                            binding.etConfirmNewPassword.error =
                                                                "Password doesn't matches"
                                                        } else {
                                                            val jsonOTPParams = JSONObject()
                                                            jsonOTPParams.put("mobile_number",
                                                                phoneNumber)
                                                            jsonOTPParams.put("password",
                                                                newPassword)
                                                            jsonOTPParams.put("otp",
                                                                otp)

                                                            binding.progressBar.visibility =
                                                                View.VISIBLE

                                                            val jsonObjectRequest =
                                                                object :
                                                                    JsonObjectRequest(
                                                                        Request.Method.POST,
                                                                        RESET_PASSWORD,
                                                                        jsonParams,
                                                                        Response.Listener<JSONObject> {
                                                                            println(it.toString())
                                                                            try {
                                                                                binding.progressBar.visibility =
                                                                                    View.GONE // This will hide the progress bar when the content is loaded.

                                                                                data =
                                                                                    it.getJSONObject(
                                                                                        "data")
                                                                                success =
                                                                                    data.getBoolean(
                                                                                        "success")
                                                                                if (success) {
                                                                                    val message =
                                                                                        data.getString(
                                                                                            "successMessage")


//                                                                                Clearing the Shared Preferences
                                                                                    val editor: SharedPreferences.Editor =
                                                                                        sharedPreferences.edit()
                                                                                    editor.clear()
                                                                                    editor.apply()

                                                                                    Toast.makeText(
                                                                                        this,
                                                                                        message,
                                                                                        Toast.LENGTH_SHORT)
                                                                                        .show()

                                                                                    startActivity(
                                                                                        Intent(
                                                                                            this,
                                                                                            LoginActivity::class.java))
                                                                                    finishAffinity()
                                                                                    binding.progressBar.visibility =
                                                                                        View.GONE

                                                                                } else {
                                                                                    Toast.makeText(
                                                                                        this,
                                                                                        "Some Error Occurred",
                                                                                        Toast.LENGTH_SHORT)
                                                                                        .show()
                                                                                }
                                                                            } catch (e: JSONException) {
                                                                                e.printStackTrace()
                                                                            }

                                                                        },
                                                                        Response.ErrorListener {
                                                                            Toast.makeText(this,
                                                                                it?.message,
                                                                                Toast.LENGTH_SHORT)
                                                                                .show()
                                                                        }) {
                                                                    override fun getHeaders(): MutableMap<String, String> {
                                                                        val headers =
                                                                            HashMap<String, String>()
                                                                        headers["Content-type"] =
                                                                            "application/json"
                                                                        headers["token"] =
                                                                            "9bf534118365f1"
                                                                        return headers
                                                                    }
                                                                }
                                                            queue.add(jsonObjectRequest)
                                                        }
                                                    }
                                                }

                                            }

                                        } else {
                                            Toast.makeText(this,
                                                "Some Error Occurred",
                                                Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }

                                },
                                Response.ErrorListener {
                                    Toast.makeText(this,
                                        it?.message,
                                        Toast.LENGTH_SHORT)
                                        .show()
                                }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-type"] = "application/json"
                                headers["token"] = "9bf534118365f1"
                                return headers
                            }
                        }
                    queue.add(jsonObjectRequest) // Adding the JSON object request
                } else {
                    //                Internet is not available
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("No Internet Connectivity") // Setting the Title for the Dialog Box
                    dialog.setMessage("Please check your internet connection and try again") // Setting message for the Dialog Box
                    dialog.setPositiveButton("Open Settings")
                    { _, _ ->
//                This will open the Wireless Settings on the Phone
                        val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingIntent)
                        finish()
                    }
                    dialog.setNegativeButton("Exit")
                    { _, _ ->
                        ActivityCompat.finishAffinity(this) // This will exit the application and finish all the activities
                    }
                    dialog.setCancelable(false)
                    dialog.create()
                    dialog.show()
                }
            }
        }

    }

}