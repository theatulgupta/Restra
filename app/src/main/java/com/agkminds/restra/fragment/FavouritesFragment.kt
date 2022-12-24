package com.agkminds.restra.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.agkminds.restra.R
import com.agkminds.restra.adapters.RestaurantAdapter
import com.agkminds.restra.database.RestaurantDB
import com.agkminds.restra.database.RestaurantEntity
import com.agkminds.restra.databinding.FragmentFavouritesBinding
import com.agkminds.restra.model.Restaurant

class FavouritesFragment : Fragment() {
    private lateinit var binding: FragmentFavouritesBinding
    private var restaurantList = arrayListOf<Restaurant>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        val view = inflater.inflate(R.layout.recycler_home_single_row, container, false)

        binding.progressBar.visibility = View.VISIBLE


        /*In case of favourites, simply extract all the data from the DB and send to the adapter.
* Here we can reuse the adapter created for the home fragment. This will save our time and optimize our app as well*/
        val backgroundList = FavouritesAsync(activity as Context).execute().get()
        if (backgroundList.isEmpty()) {
            binding.progressBar.visibility = View.GONE
            binding.rlEmptyFav.visibility = View.VISIBLE
        } else {
            binding.rlEmptyFav.visibility = View.GONE
            for (i in backgroundList) {
                restaurantList.add(
                    Restaurant(
                        i.res_id,
                        i.resName,
                        i.resRating,
                        i.resPrice.toInt(),
                        i.imageUrl
                    )
                )
            }


            if (activity != null) {
                binding.progressBar.visibility = View.GONE

                binding.favRecyclerView.adapter =
                    RestaurantAdapter(activity as Context, restaurantList)
                binding.favRecyclerView.layoutManager = LinearLayoutManager(activity as Context)
                binding.favRecyclerView.itemAnimator = DefaultItemAnimator()
                binding.favRecyclerView.setHasFixedSize(true)
            }
        }
        return binding.root
    }

    /*A new async class for fetching the data from the DB*/
    class FavouritesAsync(context: Context) : AsyncTask<Void, Void, List<RestaurantEntity>>() {
        private val db =
            Room.databaseBuilder(context, RestaurantDB::class.java, "restaurant-db").build()

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): List<RestaurantEntity> {
            return db.resDao().getAllRestaurants()
        }
    }
}