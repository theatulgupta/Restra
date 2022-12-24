package com.agkminds.restra.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agkminds.restra.R
import com.agkminds.restra.model.FoodItem

class RestaurantMenuAdapter(
    val context: Context,
    private val foodItemList: ArrayList<FoodItem>,
    private val listener: OnItemClickListener,
) :
    RecyclerView.Adapter<RestaurantMenuAdapter.RestaurantMenuViewHolder>() {

    companion object {
        var isCartEmpty = true
    }

    class RestaurantMenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodName: TextView = view.findViewById(R.id.txtFoodName)
        val foodPrice: TextView = view.findViewById(R.id.txtFoodPrice)
        val serialNumber: TextView = view.findViewById(R.id.txtSerialNo)
        val btnAdd: Button = view.findViewById(R.id.btnAdd)
        val btnRemove: Button = view.findViewById(R.id.btnRemove)
    }

    interface OnItemClickListener {
        fun onAddItemClick(foodItem: FoodItem)
        fun onRemoveItemClick(foodItem: FoodItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantMenuViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.recycler_food_item_single_row, parent, false)
        return RestaurantMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantMenuViewHolder, position: Int) {
        val foodItem = foodItemList[position]
        val index = position + 1
        holder.foodName.text = foodItem.name
        holder.foodPrice.text = foodItem.cost_for_one
        holder.serialNumber.text = index.toString()

        holder.btnAdd.setOnClickListener {
            holder.btnRemove.visibility = View.VISIBLE
            holder.btnAdd.visibility = View.GONE
            listener.onAddItemClick(foodItem)
            RestaurantMenuAdapter.isCartEmpty = false
        }
        holder.btnRemove.setOnClickListener {
            holder.btnRemove.visibility = View.GONE
            holder.btnAdd.visibility = View.VISIBLE
            listener.onRemoveItemClick(foodItem)
            RestaurantMenuAdapter.isCartEmpty = false
        }

    }

    override fun getItemCount(): Int {
        return foodItemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}

