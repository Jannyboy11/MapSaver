package fr.epicanard.mapsaver.models.config

import io.circe.generic.JsonCodec

@JsonCodec
case class Options(
  pageSize: Int,
  defaultVisibility: String
)

