package com.decovista.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_layouts")
data class RoomLayoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "space_name")
    val spaceName: String,
    @ColumnInfo(name = "room_width")
    val roomWidth: Float,
    @ColumnInfo(name = "room_length")
    val roomLength: Float,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
