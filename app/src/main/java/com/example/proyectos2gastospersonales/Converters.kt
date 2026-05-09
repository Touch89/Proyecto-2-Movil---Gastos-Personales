package com.example.proyectos2gastospersonales

import androidx.room.*
import java.sql.Date

public class Converters {
    // https://stackoverflow.com/questions/50515820/android-room-error-cannot-figure-out-how-to-save-this-field-into-database
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return if (date == null) null else date.getTime()
    }

    @TypeConverter
    fun fromMovementType(value: MovementType?): String? {
        return if (value == null) null else value.name
    }

    @TypeConverter
    fun stringToMovementType(type: String?) : MovementType? {
        return if (type == null) null else MovementType.valueOf(type)
    }
}