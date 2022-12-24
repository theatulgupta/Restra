package com.agkminds.restra.activity

import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.agkminds.restra.databinding.ActivitySignUpBinding
import com.agkminds.restra.util.ConnectionManager
import com.agkminds.restra.util.REGISTER
import com.agkminds.restra.util.SessionManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodrunner.util.Validations
import org.json.JSONObject


class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    lateinit var sessionManager: SessionManager
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)


        sessionManager = SessionManager(this)
        sharedPreferences =
            this.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)


        binding.registerScrollView.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE

        // Handling SignUp Button Click
        binding.btnSignUp.setOnClickListener {

            binding.registerScrollView.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
            if (Validations.validateNameLength(binding.etUserName.text.toString())) {
                binding.etUserName.error = null
                if (Validations.validateEmail(binding.etEmailAddress.text.toString())) {
                    binding.etEmailAddress.error = null
                    if (Validations.validateMobile(binding.etPhoneNumber.text.toString())) {
                        binding.etPhoneNumber.error = null
                        if (Validations.validatePasswordLength(binding.etPassword.text.toString())) {
                            binding.etPassword.error = null
                            if (Validations.matchPassword(
                                    binding.etPassword.text.toString(),
                                    binding.etConfirmPassword.text.toString()
                                )
                            ) {
                                binding.etPassword.error = null
                                binding.etConfirmPassword.error = null
                                if (ConnectionManager().isNetworkAvailable(this)) {
                                    sendRegisterRequest(
                                        binding.etUserName.text.toString(),
                                        binding.etPhoneNumber.text.toString(),
                                        binding.etDeliveryAddress.text.toString(),
                                        binding.etPassword.text.toString(),
                                        binding.etEmailAddress.text.toString()
                                    )
                                } else {
                                    binding.registerScrollView.visibility = View.VISIBLE
                                    binding.progressBar.visibility = View.INVISIBLE
                                    Toast.makeText(this,
                                        "No Internet Connection",
                                        Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                binding.registerScrollView.visibility = View.VISIBLE
                                binding.progressBar.visibility = View.INVISIBLE
                                binding.etPassword.error = "Passwords don't match"
                                binding.etConfirmPassword.error = "Passwords don't match"
                                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            binding.registerScrollView.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.INVISIBLE
                            binding.etPassword.error =
                                "Password should be more than or equal 4 digits"
                            Toast.makeText(
                                this,
                                "Password should be more than or equal 4 digits",
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        binding.registerScrollView.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.etPhoneNumber.error = "Invalid Mobile number"
                        Toast.makeText(this, "Invalid Mobile number",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.registerScrollView.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.etEmailAddress.error = "Invalid Email"
                    Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.registerScrollView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.INVISIBLE
                binding.etUserName.error = "Invalid Name"
                Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show()
            }

        }

        //                Handling Back Button Click
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, WelcomeScreen::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            finish()
        }
        binding.btnSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            finish()
        }
    }

    private fun sendRegisterRequest(
        name: String,
        phone: String,
        address: String,
        password: String,
        email: String,
    ) {

        val queue = Volley.newRequestQueue(this)

        val jsonParams = JSONObject()
        jsonParams.put("name", name)
        jsonParams.put("mobile_number", phone)
        jsonParams.put("password", password)
        jsonParams.put("address", address)
        jsonParams.put("email", email)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            REGISTER,
            jsonParams,
            Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val response = data.getJSONObject("data")
                        setSharedPreferences(response)
                        startActivity(
                            Intent(this, MainActivity::class.java
                            )
                        )
                        finish()
                    } else {
                        binding.progressBar.visibility = View.INVISIBLE
                        val errorMessage = data.getString("errorMessage")
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    binding.progressBar.visibility = View.INVISIBLE
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.INVISIBLE
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"

                /*The below used token will not work, kindly use the token provided to you in the training*/
                headers["token"] = "9bf534118365f1"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    fun setSharedPreferences(user_data: JSONObject) {
        sharedPreferences.edit()
            .putString("user_id", user_data.getString("user_id")).apply()
        sharedPreferences.edit()
            .putString("user_name", user_data.getString("name")).apply()
        sharedPreferences.edit()
            .putString(
                "user_mobile_number",
                user_data.getString("mobile_number")
            )
            .apply()
        sharedPreferences.edit()
            .putString("user_address", user_data.getString("address"))
            .apply()
        sharedPreferences.edit()
            .putString("user_email", user_data.getString("email")).apply()
        sessionManager.setLogin(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
        onBackPressed()
        return true
    }

}