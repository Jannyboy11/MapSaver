package fr.epicanard.mapsaver.models.config

import io.circe.generic.JsonCodec

@JsonCodec
case class Connection(
    host: String,
    port: Int,
    database: String,
    user: String,
    password: String,
    useSSL: Boolean
)
