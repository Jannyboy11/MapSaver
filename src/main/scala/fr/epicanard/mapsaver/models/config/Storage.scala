package fr.epicanard.mapsaver.models.config

import io.circe.generic.JsonCodec

@JsonCodec
case class Storage(
    `type`: DatabaseType,
    tablePrefix: String,
    connection: Connection
)
