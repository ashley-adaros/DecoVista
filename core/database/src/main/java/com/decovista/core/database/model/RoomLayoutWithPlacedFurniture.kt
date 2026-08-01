package com.decovista.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Agrupa la información de un mueble colocado con los detalles de su catálogo (dimensiones, malla, preview).
 */
data class PlacedFurnitureWithDetails(
    @Embedded 
    val placedFurniture: PlacedFurnitureEntity,
    
    @Relation(
        parentColumn = "furniture_id",
        entityColumn = "id"
    )
    val furnitureItem: FurnitureItemEntity
)

/**
 * Obtiene la habitación completa con todos los muebles colocados en ella y sus respectivos detalles técnicos.
 */
data class RoomLayoutWithPlacedFurniture(
    @Embedded 
    val roomLayout: RoomLayoutEntity,
    
    @Relation(
        entity = PlacedFurnitureEntity::class,
        parentColumn = "id",
        entityColumn = "layout_id"
    )
    val placedFurnitureList: List<PlacedFurnitureWithDetails>
)
