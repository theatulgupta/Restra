package com.agkminds.restra.adapters

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.agkminds.restra.R
import com.agkminds.restra.activity.RestaurantDetailActivity
import com.agkminds.restra.database.RestaurantDB
import com.agkminds.restra.model.Restaurant
import com.agkminds.restra.database.RestaurantEntity
import com.squareup.picasso.Picasso

class RestaurantAdapter(
    val context: Context,
    private val restaurantList: ArrayList<Restaurant>,
) :
    RecyclerView.Adapter<RestaurantAdapter.HomeViewHolder>() {
    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val restaurantName: TextView? = view.findViewById(R.id.txtRestaurantName)
        val rating: TextView? = view.findViewById(R.id.txtRating)
        val price: TextView? = view.findViewById(R.id.txtPrice)
        val image: ImageView = view.findViewById(R.id.imgRestaurant)
        val favBtn: ImageView = view.findViewById(R.id.imgFavourite)
        val resCard: CardView = view.findViewById(R.id.restaurantCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.recycler_home_single_row, parent, false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val restaurant = restaurantList[position]
        var isFavChecked = false
        val modPrice = "${restaurant.cost_for_one}/person"

        holder.restaurantName!!.text = restaurant.name
        holder.rating!!.text = restaurant.rating
        holder.price!!.text = modPrice
        Picasso.get().load(restaurant.image_url).error(R.drawable.ic_food).into(holder.image)

        val listOfFavourites = GetAllFavAsyncTask(context).execute().get()

        isFavChecked =
            if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(restaurant.id.toString())) {
                holder.favBtn.setImageResource(R.drawable.ic_fav_checked)
                true
            } else {
                holder.favBtn.setImageResource(R.drawable.ic_fav_unchecked)
                false
            }

        holder.favBtn.setOnClickListener {
            val restaurantEntity = RestaurantEntity(
                restaurant.id,
                restaurant.name,
                restaurant.rating,
                restaurant.cost_for_one.toString(),
                restaurant.image_url
            )

            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val async =
                    DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()
                if (result) {
                    holder.favBtn.setImageResource(R.drawable.ic_fav_checked)
                    isFavChecked = true
                }
            } else {
                val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                val result = async.get()

                if (result) {
                    holder.favBtn.setImageResource(R.drawable.ic_fav_unchecked)
                    isFavChecked = false
                }
            }
        }

        holder.resCard.setOnClickListener {
            val intent = Intent(context, RestaurantDetailActivity::class.java)
            intent.putExtra("isFavChecked", isFavChecked)
            intent.putExtra("res_id", restaurant.id)
            intent.putExtra("res_name", restaurant.name)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return restaurantList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}

class DBAsyncTask(
    context: Context,
    private val restaurantEntity: RestaurantEntity,
    private val mode: Int,
) :
    AsyncTask<Void, Void, Boolean>() {

    private val db =
        Room.databaseBuilder(context, RestaurantDB::class.java, "restaurant-db").build()


    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): Boolean {

/*
Mode 1 -> Check DB if the book is favourite or not
Mode 2 -> Save the book into DB as favourite
Mode 3 -> Remove the favourite book
*/

        when (mode) {
            1 -> {
                val res: RestaurantEntity? =
                    db.resDao().getRestaurantById(restaurantEntity.res_id.toString())
                db.close()
                return res != null
            }
            2 -> {
                db.resDao().insertRestaurant(restaurantEntity)
                db.close()
                return true
            }
            3 -> {
                db.resDao().deleteRestaurant(restaurantEntity)
                db.close()
                return true
            }
        }
        return false
    }
}


class GetAllFavAsyncTask(context: Context) :
    AsyncTask<Void, Void, List<String>>() {
    private val db =
        Room.databaseBuilder(context, RestaurantDB::class.java, "restaurant-db").build()

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): List<String> {


        val restaurantList = db.resDao().getAllRestaurants()
        val listOfIds = arrayListOf<String>()
        for (i in restaurantList) {
            listOfIds.add(i.res_id.toString())
        }
        return listOfIds
    }
}