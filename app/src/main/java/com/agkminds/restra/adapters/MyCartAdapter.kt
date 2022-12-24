package com.agkminds.restra.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agkminds.restra.R
import com.agkminds.restra.model.FoodItem

class MyCartAdapter(val context: Context, private val foodItemList: ArrayList<FoodItem>) :
    RecyclerView.Adapter<MyCartAdapter.MyCartViewHolder>() {

    class MyCartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cartFoodName: TextView = view.findViewById(R.id.txtCartFoodName)
        val cartFoodPrice: TextView = view.findViewById(R.id.txtCartFoodPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCartViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.recycler_cart_item_single_row, parent, false)

        return MyCartViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyCartViewHolder, position: Int) {
        val foodItem = foodItemList[position]
        val foodCost = "Rs. ${foodItem.cost_for_one}"

        holder.cartFoodName.text = foodItem.name
        holder.cartFoodPrice.text = foodCost
    }

    override fun getItemCount(): Int {
        return foodItemList.size
    }
}
