package com.agkminds.restra.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.agkminds.restra.R
import com.agkminds.restra.adapters.OrderHistoryAdapter
import com.agkminds.restra.databinding.FragmentOrderHistoryBinding
import com.agkminds.restra.model.OrderDetails
import com.agkminds.restra.util.FETCH_PREVIOUS_ORDERS
import com.agkminds.restra.util.SessionManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class OrderHistoryFragment : Fragment() {
    lateinit var binding: FragmentOrderHistoryBinding
    private var orderHistoryList = ArrayList<OrderDetails>()
    lateinit var sessionManager: SessionManager
    private lateinit var sharedPreferences: SharedPreferences
    private var userId = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        binding.progressBar.visibility = View.VISIBLE

        sessionManager = SessionManager(activity as Context)
        sharedPreferences =
            (activity as Context).getSharedPreferences(sessionManager.PREF_NAME,
                sessionManager.PRIVATE_MODE)

        userId = sharedPreferences.getString("user_id", "")!!

        val queue = Volley.newRequestQueue(activity as Context)


        val jsonObjectRequest = object :
            JsonObjectRequest(Method.GET, FETCH_PREVIOUS_ORDERS + userId, null, Response.Listener {
                binding.rlLoading.visibility = View.GONE
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {

                        binding.progressBar.visibility = View.GONE

                        val resArray = data.getJSONArray("data")
                        if (resArray.length() == 0) {
                            binding.llHasOrders.visibility = View.GONE
                            binding.rlNoOrders.visibility = View.VISIBLE
                        } else {
                            for (i in 0 until resArray.length()) {
                                val orderObject = resArray.getJSONObject(i)
                                val foodItems = orderObject.getJSONArray("food_items")
                                val orderDetails = OrderDetails(
                                    orderObject.getInt("order_id"),
                                    orderObject.getString("restaurant_name"),
                                    orderObject.getString("order_placed_at"),
                                    foodItems
                                )
                                orderHistoryList.add(orderDetails)

                                if (orderHistoryList.isEmpty()) {
                                    binding.llHasOrders.visibility = View.GONE
                                    binding.rlNoOrders.visibility = View.VISIBLE
                                } else {
                                    binding.llHasOrders.visibility = View.VISIBLE

                                    if (activity != null) {
                                        binding.recyclerOrderHistory.adapter = OrderHistoryAdapter(
                                            activity as Context,
                                            orderHistoryList)
                                        binding.recyclerOrderHistory.layoutManager =
                                            LinearLayoutManager(activity as Context)
                                        binding.recyclerOrderHistory.itemAnimator =
                                            DefaultItemAnimator()
                                    } else {
                                        queue.cancelAll(this::class.java.simpleName)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener {
                Toast.makeText(activity as Context, it.message, Toast.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "9bf534118365f1"
                return headers
            }
        }
        queue.add(jsonObjectRequest)

        return binding.root
    }
}