package com.agkminds.restra.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.agkminds.restra.R
import com.agkminds.restra.adapters.RestaurantMenuAdapter
import com.agkminds.restra.database.OrderEntity
import com.agkminds.restra.database.RestaurantDB
import com.agkminds.restra.databinding.ActivityRestaurantDetailBinding
import com.agkminds.restra.model.FoodItem
import com.agkminds.restra.util.ConnectionManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import kotlin.properties.Delegates


class RestaurantDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityRestaurantDetailBinding
    private var resId = 100
    private lateinit var resName: String
    val menuList = arrayListOf<FoodItem>()
    val orderList = arrayListOf<FoodItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            ActivityRestaurantDetailBinding.inflate(LayoutInflater.from(
                this))
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Restra"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent != null) {
            val isFavChecked = intent.getBooleanExtra("isFavChecked", false)
            if (isFavChecked) {
                binding.imgFavourite.setImageResource(R.drawable.ic_fav_checked)
            }

            resName = intent.getStringExtra("res_name").toString()
            supportActionBar?.title = resName

            resId = intent.getIntExtra("res_id", 100)

            if (resId == 100) {
                Toast.makeText(this, "Some unexpected error occurred!!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Some unexpected error occurred!!", Toast.LENGTH_SHORT).show()
            finish()
        }

        val queue = Volley.newRequestQueue(this)

        if (ConnectionManager().isNetworkAvailable(this)) {
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId"

            //        Progress bar displaying
            binding.progressBar.visibility = View.VISIBLE

            val jsonObjectRequest =
                object :
                    JsonObjectRequest(Request.Method.GET,
                        url,
                        null,
                        Response.Listener<JSONObject> {
                            println(it.toString())
                            try {
                                binding.progressBar.visibility =
                                    View.GONE // This will hide the progress bar when the content is loaded.
                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")
                                if (success) {
                                    val foodArray = data.getJSONArray("data")
                                    for (i in 0 until foodArray.length()) {
                                        val foodObject = foodArray.getJSONObject(i)
                                        val foodItem = FoodItem(
                                            foodObject.getString("id").toInt(),
                                            foodObject.getString("name"),
                                            foodObject.getString("cost_for_one"),
                                        )

                                        menuList.add(foodItem) // Adding Parsed data to bookInfoList

                                        // Specifying the LayoutManager of the Recycler View
                                        binding.detailRecyclerView.layoutManager =
                                            LinearLayoutManager(this)

                                        // Setting adapter for the Recycler View
                                        binding.detailRecyclerView.adapter =
                                            RestaurantMenuAdapter(this,
                                                menuList,
                                                object : RestaurantMenuAdapter.OnItemClickListener {
                                                    override fun onAddItemClick(foodItem: FoodItem) {
                                                        orderList.add(foodItem)
                                                        println(orderList)
                                                        if (orderList.size > 0) {
                                                            RestaurantMenuAdapter.isCartEmpty =
                                                                false
                                                            binding.btnProceedToCart.visibility =
                                                                View.VISIBLE
                                                        }
                                                    }

                                                    override fun onRemoveItemClick(foodItem: FoodItem) {
                                                        orderList.remove(foodItem)
                                                        if (orderList.isEmpty()) {
                                                            RestaurantMenuAdapter.isCartEmpty =
                                                                true
                                                            binding.btnProceedToCart.visibility =
                                                                View.GONE
                                                        }
                                                    }
                                                })

//                                            Setting Default Item Animator
                                        binding.detailRecyclerView.itemAnimator =
                                            DefaultItemAnimator()

//                                            Setting fixed size of Recycler View to Increase Performance
                                        binding.detailRecyclerView.setHasFixedSize(true)

//                                        binding.detailRecyclerView.addItemDecoration(
//                                            DividerItemDecoration(this,
//                                                DividerItemDecoration.VERTICAL))

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
                            Toast.makeText(this, it?.message, Toast.LENGTH_SHORT)
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
            dialog.setPositiveButton("Open Settings") { _, _ ->
//                This will open the Wireless Settings on the Phone
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this) // This will exit the application and finish all the activities
            }
            dialog.setCancelable(false)
            dialog.create()
            dialog.show()
        }

        binding.btnProceedToCart.setOnClickListener {
            /*Here we see the implementation of Gson.
            * Whenever we want to convert the custom data types into simple data types
            * which can be transferred across for utility purposes, we will use Gson*/
            val gson = Gson()

            /*With the below code, we convert the list of order items into simple string which can be easily stored in DB*/
            val foodItems = gson.toJson(orderList)


            val async = ItemsOfCart(this, resId, foodItems, 1).execute()
            val result = async.get()
            if (result) {
                val data = Bundle()
                data.putInt("resId", resId.toInt())
                data.putString("resName", resName)
                val intent = Intent(this, MyCartActivity::class.java)
                intent.putExtra("data", data)
                startActivity(intent)
            } else {
                Toast.makeText((this), "Some unexpected error", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }


    //      This will enable the function of back key
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                RemoveOrders(this, resId)
                orderList.clear()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

class ItemsOfCart(
    context: Context,
    private val restaurantId: Int,
    private val foodItems: String,
    private val mode: Int,
) : AsyncTask<Void, Void, Boolean>() {
    private val db =
        Room.databaseBuilder(context, RestaurantDB::class.java, "restaurant-db").build()


    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): Boolean {
        when (mode) {
            1 -> {
                db.orderDao().insertOrder(OrderEntity(restaurantId, foodItems))
                db.close()
                return true
            }

            2 -> {
                db.orderDao().deleteOrder(OrderEntity(restaurantId, foodItems))
                db.close()
                return true
            }
        }
        return false
    }
}

class RemoveOrders(context: Context, private val restaurantId: Int) :
    AsyncTask<Void, Void, Boolean>() {
    val db =
        Room.databaseBuilder(context,
            RestaurantDB::class.java,
            "restaurant-db").build()

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): Boolean {
        db.orderDao().deleteOrders(restaurantId)
        db.close()
        return true
    }

}

