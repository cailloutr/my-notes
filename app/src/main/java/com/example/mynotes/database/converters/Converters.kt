package com.example.mynotes.database.converters

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun toInteger(boolean: Boolean): Int {
        return if (boolean) 1 else 0
    }

    @TypeConverter
    fun integerToBoolean(value: Int): Boolean {
        return value == 1
    }
}