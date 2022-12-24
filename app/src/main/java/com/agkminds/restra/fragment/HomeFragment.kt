package com.agkminds.restra.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.agkminds.restra.R
import com.agkminds.restra.model.Restaurant
import com.agkminds.restra.adapters.RestaurantAdapter
import com.agkminds.restra.databinding.FragmentHomeBinding
import com.agkminds.restra.util.ConnectionManager
import com.agkminds.restra.util.FETCH_RESTAURANTS
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    val restaurantList = arrayListOf<Restaurant>()
    private val ratingComparator = Comparator<Restaurant> { res1, res2 ->
        if (res1.rating.compareTo(res2.rating, true) == 0) {
            res1.name.compareTo(res2.name, true)
        } else {
            res1.rating.compareTo(res2.rating, true)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        //        This code will set the Sort Icon in the appBar
        setHasOptionsMenu(true)

        if (ConnectionManager().isNetworkAvailable(activity as Context)) {

//        Using Volley-Library to fetch JSON objects from API
            val queue = Volley.newRequestQueue(activity as Context)

//        Progress bar displaying
            binding.progressBar.visibility = View.VISIBLE

            val jsonObjectRequest =
                object :
                    JsonObjectRequest(Request.Method.GET,
                        FETCH_RESTAURANTS,
                        null,
                        Response.Listener<JSONObject> {
                            println(it.toString())
                            try {

                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")
                                if (success) {
                                    val resArray = data.getJSONArray("data")
                                    for (i in 0 until resArray.length()) {
                                        val resObject = resArray.getJSONObject(i)
                                        val restaurant = Restaurant(
                                            resObject.getString("id").toInt(),
                                            resObject.getString("name"),
                                            resObject.getString("rating"),
                                            resObject.getString("cost_for_one").toInt(),
                                            resObject.getString("image_url"),
                                        )
                                        restaurantList.add(restaurant) // Adding Parsed data to bookInfoList

                                        if (activity != null) {

                                            // Specifying the LayoutManager of the Recycler View
                                            binding.recyclerView.layoutManager =
                                                LinearLayoutManager(activity)

                                            // Setting adapter for the Recycler View
                                            binding.recyclerView.adapter =
                                                RestaurantAdapter(activity as Context,
                                                    restaurantList)

//                                            Setting Default Item Animator
                                            binding.recyclerView.itemAnimator =
                                                DefaultItemAnimator()

//                                            Setting fixed size of Recycler View to Increase Performance
                                            binding.recyclerView.setHasFixedSize(true)
                                        }
                                        binding.progressBar.visibility =
                                            View.GONE // This will hide the progress bar when the content is loaded.
                                    }
                                } else {
                                    Toast.makeText(activity as Context,
                                        "Some Error Occurred",
                                        Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                        },
                        Response.ErrorListener {
                            Toast.makeText(activity as Context, it?.message, Toast.LENGTH_SHORT)
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
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("No Internet Connectivity") // Setting the Title for the Dialog Box
            dialog.setMessage("Please check your internet connection and try again") // Setting message for the Dialog Box
            dialog.setPositiveButton("Open Settings") { _, _ ->
//                This will open the Wireless Settings on the Phone
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity) // This will exit the application and finish all the activities
            }
            dialog.setCancelable(false)
            dialog.create()
            dialog.show()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        if (id == R.id.action_sort) {
            Collections.sort(restaurantList, ratingComparator)
            restaurantList.reverse()
        }

        binding.recyclerView.adapter?.notifyDataSetChanged()
        return super.onOptionsItemSelected(item)
    }

}