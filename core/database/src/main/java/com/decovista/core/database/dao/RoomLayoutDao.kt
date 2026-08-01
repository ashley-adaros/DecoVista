package com.decovista.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.decovista.core.database.model.PlacedFurnitureEntity
import com.decovista.core.database.model.RoomLayoutEntity
import com.decovista.core.database.model.RoomLayoutWithPlacedFurniture
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomLayoutDao {

    // --- Operaciones sobre RoomLayout ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomLayout(layout: RoomLayoutEntity): Long

    @Update
    suspend fun updateRoomLayout(layout: RoomLayoutEntity)

    @Delete
    suspend fun deleteRoomLayout(layout: RoomLayoutEntity)

    @Query("SELECT * FROM room_layouts ORDER BY created_at DESC")
    fun getAllRoomLayouts(): Flow<List<RoomLayoutEntity>>

    // --- Operaciones sobre PlacedFurniture ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacedFurniture(placed: PlacedFurnitureEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacedFurnitureList(placedList: List<PlacedFurnitureEntity>)

    @Update
    suspend fun updatePlacedFurniture(placed: PlacedFurnitureEntity)

    @Delete
    suspend fun deletePlacedFurniture(placed: PlacedFurnitureEntity)

    @Query("DELETE FROM placed_furniture WHERE layout_id = :layoutId")
    suspend fun clearFurnitureFromLayout(layoutId: Long)

    // --- Consultas Relacionales complejas ---

    @Transaction
    @Query("SELECT * FROM room_layouts WHERE id = :layoutId")
    fun getRoomLayoutWithFurniture(layoutId: Long): Flow<RoomLayoutWithPlacedFurniture?>

    @Transaction
    @Query("SELECT * FROM room_layouts")
    fun getAllRoomLayoutsWithFurniture(): Flow<List<RoomLayoutWithPlacedFurniture>>

    /**
     * Reemplaza todos los muebles de un plano en una sola transacción.
     */
    @Transaction
    suspend fun updateLayoutFurnitureTransaction(layoutId: Long, newList: List<PlacedFurnitureEntity>) {
        clearFurnitureFromLayout(layoutId)
        insertPlacedFurnitureList(newList.map { it.copy(layoutId = layoutId) })
    }
}
