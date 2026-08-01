package com.decovista.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "placed_furniture",
    foreignKeys = [
        ForeignKey(
            entity = RoomLayoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["layout_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FurnitureItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["furniture_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["layout_id"]),
        Index(value = ["furniture_id"])
    ]
)
data class PlacedFurnitureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "layout_id")
    val layoutId: Long,
    @ColumnInfo(name = "furniture_id")
    val furnitureId: String,
    @ColumnInfo(name = "position_x")
    val positionX: Float,
    @ColumnInfo(name = "position_y")
    val positionY: Float,
    @ColumnInfo(name = "position_z")
    val positionZ: Float,
    val rotation: Float
)
