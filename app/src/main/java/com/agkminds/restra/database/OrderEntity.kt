package com.agkminds.restra.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val res_id: Int,
    @ColumnInfo(name = "food_item") val foodItem: String,
)
