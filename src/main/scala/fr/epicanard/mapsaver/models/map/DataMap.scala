package fr.epicanard.mapsaver.models.map

import java.sql.Timestamp

case class DataMap(
    id: Int,
    bytes: Array[Byte],
    createdAt: Timestamp,
    updatedAt: Timestamp
)
