package com.decovista.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.decovista.core.database.dao.FurnitureDao
import com.decovista.core.database.dao.RoomLayoutDao
import com.decovista.core.database.model.FurnitureItemEntity
import com.decovista.core.database.model.PlacedFurnitureEntity
import com.decovista.core.database.model.RoomLayoutEntity

@Database(
    entities = [
        FurnitureItemEntity::class,
        RoomLayoutEntity::class,
        PlacedFurnitureEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DecoVistaDatabase : RoomDatabase() {

    abstract fun furnitureDao(): FurnitureDao
    abstract fun roomLayoutDao(): RoomLayoutDao

    companion object {
        @Volatile
        private var INSTANCE: DecoVistaDatabase? = null

        private const val DATABASE_NAME = "decovista_db"

        /**
         * Método estático clásico para inicializar la base de datos en caso de no utilizar 
         * un framework de inyección de dependencias (Hilt/Koin).
         */
        fun getDatabase(context: Context): DecoVistaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DecoVistaDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // Útil durante las fases iniciales de desarrollo
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
