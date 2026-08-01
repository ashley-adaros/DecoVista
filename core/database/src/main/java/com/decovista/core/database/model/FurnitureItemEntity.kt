package com.decovista.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "furniture_items")
data class FurnitureItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    @ColumnInfo(name = "width_meters")
    val widthMeters: Float,
    @ColumnInfo(name = "height_meters")
    val heightMeters: Float,
    @ColumnInfo(name = "depth_meters")
    val depthMeters: Float,
    @ColumnInfo(name = "unit_of_measurement")
    val unitOfMeasurement: String,
    @ColumnInfo(name = "model_3d_url")
    val model3dUrl: String,
    @ColumnInfo(name = "preview_image_url")
    val previewImageUrl: String
)
