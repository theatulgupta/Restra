package com.agkminds.restra.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.agkminds.restra.R
import com.agkminds.restra.adapters.MyCartAdapter
import com.agkminds.restra.adapters.RestaurantMenuAdapter
import com.agkminds.restra.database.OrderEntity
import com.agkminds.restra.database.RestaurantDB
import com.agkminds.restra.databinding.ActivityMyCartBinding
import com.agkminds.restra.fragment.HomeFragment
import com.agkminds.restra.model.FoodItem
import com.agkminds.restra.util.PLACE_ORDER
import com.agkminds.restra.util.SessionManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject


class MyCartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyCartBinding
    private var orderList = ArrayList<FoodItem>()
    private var resId: Int = 0
    private var resName: String = ""
    private var userId = ""
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        sharedPreferences =
            this.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)

        userId = sharedPreferences.getString("user_id", "")!!

//        Setting up Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.getBundleExtra("data")
        resId = bundle?.getInt("resId", 0) as Int
        resName = "Ordering From: ${bundle.getString("resName", "") as String}"

        binding.txtRestaurantName.text = resName
        val dbList = GetOrders(applicationContext).execute().get()

        /*Extracting the data saved in database and then using Gson to convert the String of food items into a list
        * of food items*/
        for (element in dbList) {
            orderList.addAll(
                Gson().fromJson(element.foodItem, Array<FoodItem>::class.java).asList()
            )
        }

        /*If the order list extracted from DB is empty we do not display the cart*/
        if (orderList.isEmpty()) {
            binding.cartRecyclerView.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.cartRecyclerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }

        /*Else we display the cart using the cart item adapter*/
        binding.cartRecyclerView.adapter =
            MyCartAdapter(this, orderList)
        binding.progressBar.visibility = View.GONE
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.cartRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.cartRecyclerView.setHasFixedSize(true)

        /*Before placing the order, the user is displayed the price or the items on the button for placing the orders*/
        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].cost_for_one.toInt()
        }
        val total = "Place Order(Total: Rs. $sum)"
        binding.btnPlaceOrder.text = total

        binding.btnPlaceOrder.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            binding.cartRecyclerView.visibility = View.INVISIBLE

            val queue = Volley.newRequestQueue(this)

            /*Creating the json object required for placing the order*/
            val jsonParams = JSONObject()
            jsonParams.put("user_id", userId)
            jsonParams.put("restaurant_id", resId)

            jsonParams.put("total_cost", sum.toString())
            val foodArray = JSONArray()
            for (i in 0 until orderList.size) {
                val foodId = JSONObject()
                foodId.put("food_item_id", orderList[i].id)
                foodArray.put(i, foodId)
            }
            jsonParams.put("food", foodArray)

            val jsonObjectRequest =
                object : JsonObjectRequest(Method.POST, PLACE_ORDER, jsonParams, Response.Listener {

                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        /*If order is placed, clear the DB for the recently added items
                        * Once the DB is cleared, notify the user that the order has been placed*/
                        if (success) {
                            val clearCart = ClearCart(applicationContext, resId).execute().get()

                            RestaurantMenuAdapter.isCartEmpty = true
                            binding.cartRecyclerView.visibility = View.GONE
                            binding.displayOrderConfirm.visibility = View.VISIBLE

                            binding.btnShopMore.setOnClickListener {
                                startActivity(Intent(this,
                                    MainActivity::class.java))
                                ActivityCompat.finishAffinity(this)
                            }
                        } else {
                            binding.cartRecyclerView.visibility = View.VISIBLE
                            Toast.makeText(this,
                                "Some Error occurred",
                                Toast.LENGTH_SHORT)
                                .show()
                        }

                    } catch (e: Exception) {
                        binding.cartRecyclerView.visibility = View.VISIBLE
                        e.printStackTrace()
                    }

                }, Response.ErrorListener {
                    binding.cartRecyclerView.visibility = View.VISIBLE
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"

                        //The below used token will not work, kindly use the token provided to you in the training
                        headers["token"] = "9bf534118365f1"
                        return headers
                    }
                }

            queue.add(jsonObjectRequest)
        }
    }

    //    /*When the user presses back, we clear the cart so that when the returns to the cart, there is no
//    * redundancy in the entries*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                ClearCart(applicationContext, resId).execute().get()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /*A new async class for fetching the data from the DB*/
    class GetOrders(context: Context) : AsyncTask<Void, Void, List<OrderEntity>>() {
        private val db =
            Room.databaseBuilder(context, RestaurantDB::class.java, "restaurant-db").build()

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            return db.orderDao().getAllOrders()
        }
    }


    /*Asynctask class for clearing the recently added items from the database*/
    class ClearCart(context: Context, private val restaurantId: Int) :
        AsyncTask<Void, Void, Boolean>() {
        private val db =
            Room.databaseBuilder(context, RestaurantDB::class.java, "restaurant-db").build()

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg p0: Void?): Boolean {
            db.orderDao().deleteOrders(restaurantId)
            db.close()
            return true
        }
    }

    override fun onPause() {
        ClearCart(applicationContext, resId).execute().get()
        super.onPause()
    }

    override fun onDestroy() {
        ClearCart(applicationContext, resId).execute().get()
        super.onDestroy()
    }
}




