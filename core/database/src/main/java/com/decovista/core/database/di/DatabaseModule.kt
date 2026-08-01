package com.decovista.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.decovista.core.database.DecoVistaDatabase
import com.decovista.core.database.dao.FurnitureDao
import com.decovista.core.database.dao.RoomLayoutDao
import com.decovista.core.database.model.FurnitureItemEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun provideDecoVistaDatabase(
        @ApplicationContext context: Context,
        databaseCallback: DatabaseCallback
    ): DecoVistaDatabase {
        return Room.databaseBuilder(
            context,
            DecoVistaDatabase::class.java,
            "decovista_db"
        )
        .addCallback(databaseCallback) // Callback para pre-poblar
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideFurnitureDao(database: DecoVistaDatabase): FurnitureDao {
        return database.furnitureDao()
    }

    @Provides
    @Singleton
    fun provideRoomLayoutDao(database: DecoVistaDatabase): RoomLayoutDao {
        return database.roomLayoutDao()
    }
}

/**
 * Callback de Room para insertar automáticamente los muebles de prueba en el catálogo 
 * al momento de crear la base de datos por primera vez.
 */
class DatabaseCallback @Inject constructor(
    private val furnitureDaoProvider: Provider<FurnitureDao>,
    @ApplicationScope private val applicationScope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Insertar los elementos asíncronamente en el hilo IO
        applicationScope.launch {
            populateCatalog()
        }
    }

    private suspend fun populateCatalog() {
        val furnitureDao = furnitureDaoProvider.get()
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
