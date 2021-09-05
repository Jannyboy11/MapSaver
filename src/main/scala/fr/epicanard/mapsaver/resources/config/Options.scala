package fr.epicanard.mapsaver.resources.config

import io.circe.generic.JsonCodec

@JsonCodec
case class Options(
    pageSize: Int,
    defaultVisibility: String
)
