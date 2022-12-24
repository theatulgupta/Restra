package com.agkminds.restra.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

/* Dao for accessing the data present inside the DB*/

@Dao
interface OrderDao {

    @Insert
    fun insertOrder(orderEntity: OrderEntity)


    @Delete
    fun deleteOrder(orderEntity: OrderEntity)


    @Query("SELECT * FROM orders")
    fun getAllOrders(): List<OrderEntity>


    @Query("DELETE FROM orders WHERE res_id = :resId")
    fun deleteOrders(resId: Int)
}