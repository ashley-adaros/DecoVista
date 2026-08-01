package com.decovista.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.decovista.core.database.model.FurnitureItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FurnitureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFurnitureItem(item: FurnitureItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFurnitureItems(items: List<FurnitureItemEntity>)

    @Update
    suspend fun updateFurnitureItem(item: FurnitureItemEntity)

    @Delete
    suspend fun deleteFurnitureItem(item: FurnitureItemEntity)

    @Query("SELECT * FROM furniture_items WHERE id = :id")
    suspend fun getFurnitureItemById(id: String): FurnitureItemEntity?

    @Query("SELECT * FROM furniture_items")
    fun getAllFurnitureItems(): Flow<List<FurnitureItemEntity>>

    @Query("SELECT * FROM furniture_items WHERE category = :category")
    fun getFurnitureItemsByCategory(category: String): Flow<List<FurnitureItemEntity>>
}
