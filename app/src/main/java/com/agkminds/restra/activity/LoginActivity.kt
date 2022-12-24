package com.agkminds.restra.activity

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.agkminds.restra.R
import com.agkminds.restra.databinding.ActivityLoginBinding
import com.agkminds.restra.util.ConnectionManager
import com.agkminds.restra.util.LOGIN
import com.agkminds.restra.util.REGISTER
import com.agkminds.restra.util.SessionManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.internshala.foodrunner.util.Validations
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        sharedPreferences =
            this.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)


        binding.btnSignIn.setOnClickListener {
            /*Hide the login button when the process is going on*/
            binding.btnSignIn.visibility = View.INVISIBLE

            /*First validate the mobile number and password length*/
            if (Validations.validateMobile(binding.etPhoneNumber.text.toString()) && Validations.validatePasswordLength(
                    binding.etPassword.text.toString())
            ) {
                if (ConnectionManager().isNetworkAvailable(this@LoginActivity)) {

                    binding.progressBar.visibility = View.VISIBLE
                    /*Create the queue for the request*/
                    val queue = Volley.newRequestQueue(this@LoginActivity)

                    /*Create the JSON parameters to be sent during the login process*/
                    val jsonParams = JSONObject()
                    jsonParams.put("mobile_number", binding.etPhoneNumber.text.toString())
                    jsonParams.put("password", binding.etPassword.text.toString())


                    /*Finally send the json object request*/
                    val jsonObjectRequest =
                        object : JsonObjectRequest(Method.POST, LOGIN, jsonParams,
                            Response.Listener {

                                try {
                                    val data = it.getJSONObject("data")
                                    val success = data.getBoolean("success")
                                    if (success) {
                                        val response = data.getJSONObject("data")
                                        sharedPreferences.edit()
                                            .putString("user_id", response.getString("user_id"))
                                            .apply()
                                        sharedPreferences.edit()
                                            .putString("user_name", response.getString("name"))
                                            .apply()
                                        sharedPreferences.edit()
                                            .putString(
                                                "user_mobile_number",
                                                response.getString("mobile_number")
                                            )
                                            .apply()
                                        sharedPreferences.edit()
                                            .putString("user_address",
                                                response.getString("address"))
                                            .apply()
                                        sharedPreferences.edit()
                                            .putString("user_email", response.getString("email"))
                                            .apply()
                                        sessionManager.setLogin(true)
                                        startActivity(
                                            Intent(this, MainActivity::class.java)
                                        )
                                        finish()
                                        binding.progressBar.visibility = View.GONE
                                    } else {
                                        binding.btnForgotPassword.visibility = View.VISIBLE
                                        binding.progressBar.visibility = View.GONE
                                        binding.btnSignIn.visibility = View.VISIBLE
                                        val errorMessage = data.getString("errorMessage")
                                        Toast.makeText(
                                            this@LoginActivity,
                                            errorMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: JSONException) {
                                    binding.btnSignIn.visibility = View.VISIBLE
                                    binding.progressBar.visibility = View.GONE
                                    binding.btnForgotPassword.visibility = View.VISIBLE
                                    binding.btnSignUp.visibility = View.VISIBLE
                                    e.printStackTrace()
                                }
                            },
                            Response.ErrorListener {
                                binding.btnSignIn.visibility = View.VISIBLE
                                binding.btnForgotPassword.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.GONE
                                binding.btnSignUp.visibility = View.VISIBLE
                                Log.e("Error::::", "/post request fail! Error: ${it.message}")
                            }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-type"] = "application/json"

                                /*The below used token will not work, kindly use the token provided to you in the training*/
                                headers["token"] = "9bf534118365f1"
                                return headers
                            }
                        }
                    queue.add(jsonObjectRequest)

                } else {
                    binding.btnSignIn.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.btnForgotPassword.visibility = View.VISIBLE
                    binding.btnSignUp.visibility = View.VISIBLE
                    Toast.makeText(this@LoginActivity, "No internet Connection", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                binding.btnSignIn.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.btnForgotPassword.visibility = View.VISIBLE
                binding.btnSignUp.visibility = View.VISIBLE
                Toast.makeText(this@LoginActivity, "Invalid Phone or Password", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        //        Handling Forgot Password Click
        binding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }

//        Handling Back Button Click
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, WelcomeScreen::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            finish()
        }
    }
}