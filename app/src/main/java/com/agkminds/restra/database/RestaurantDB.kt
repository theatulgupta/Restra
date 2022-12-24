package com.agkminds.restra.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RestaurantEntity::class, OrderEntity::class], version = 1)
abstract class RestaurantDB : RoomDatabase() {
    abstract fun resDao(): RestaurantDao
    abstract fun orderDao(): OrderDao
}