package com.decovista.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.decovista.core.database.DecoVistaDatabase
import com.decovista.core.database.dao.FurnitureDao
import com.decovista.core.database.dao.RoomLayoutDao
import com.decovista.core.database.model.FurnitureItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Proveedor manual Singleton de la Base de Datos para evitar dependencias de Hilt/Kapt.
 */
object DatabaseProvider {
    @Volatile
    private var INSTANCE: DecoVistaDatabase? = null

    fun getDatabase(context: Context): DecoVistaDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                DecoVistaDatabase::class.java,
                "decovista_db"
            )
            .addCallback(DatabaseCallback(context.applicationContext))
            .fallbackToDestructiveMigration()
            .build()
            INSTANCE = instance
            instance
        }
    }
}

/**
 * Callback de Room para insertar automáticamente los muebles de prueba en el catálogo 
 * al momento de crear la base de datos por primera vez.
 */
class DatabaseCallback(
    private val appContext: Context
) : RoomDatabase.Callback() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        applicationScope.launch {
            populateCatalog()
        }
    }

    private suspend fun populateCatalog() {
        val database = DatabaseProvider.getDatabase(appContext)
        val furnitureDao = database.furnitureDao()
        
        val initialItems = listOf(
            FurnitureItemEntity(
                id = "1",
                name = "Sofá Escandinavo 3 Cuerpos",
                category = "Salón",
                widthMeters = 2.1f,
                heightMeters = 0.85f,
                depthMeters = 0.9f,
                unitOfMeasurement = "meters",
                model3dUrl = "models/sofa_nordic.glb",
                previewImageUrl = "images/previews/sofa_nordic.png"
            ),
            FurnitureItemEntity(
                id = "2",
                name = "Mesa Comedor de Roble",
                category = "Salón",
                widthMeters = 1.8f,
                heightMeters = 0.75f,
                depthMeters = 0.95f,
                unitOfMeasurement = "meters",
                model3dUrl = "models/dining_table.glb",
                previewImageUrl = "images/previews/dining_table.png"
            ),
            FurnitureItemEntity(
                id = "3",
                name = "Sillón de Lectura Velvet",
                category = "Salón",
                widthMeters = 0.85f,
                heightMeters = 1.0f,
                depthMeters = 0.8f,
                unitOfMeasurement = "meters",
                model3dUrl = "models/armchair_velvet.glb",
                previewImageUrl = "images/previews/armchair_velvet.png"
            ),
            FurnitureItemEntity(
                id = "4",
                name = "Aparador Vintage Wood",
                category = "Comedor",
                widthMeters = 1.6f,
                heightMeters = 0.75f,
                depthMeters = 0.45f,
                unitOfMeasurement = "meters",
                model3dUrl = "models/sideboard_wood.glb",
                previewImageUrl = "images/previews/sideboard_wood.png"
            ),
            FurnitureItemEntity(
                id = "5",
                name = "Librería Estilo Industrial",
                category = "Estudio",
                widthMeters = 0.9f,
                heightMeters = 1.9f,
                depthMeters = 0.35f,
                unitOfMeasurement = "meters",
                model3dUrl = "models/bookshelf_industrial.glb",
                previewImageUrl = "images/previews/bookshelf_industrial.png"
            )
        )
        furnitureDao.insertFurnitureItems(initialItems)
    }
}
