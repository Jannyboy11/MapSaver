package fr.epicanard.mapsaver.models.circe

import enumeratum.EnumEntry
import enumeratum.Enum
import io.circe.Encoder
import io.circe.Decoder
import enumeratum.Circe

trait CirceEnumInsensitive[A <: EnumEntry] { this: Enum[A] =>
  implicit val circeEncoder: Encoder[A] = Circe.encoder(this)

  implicit val circeDecoder: Decoder[A] = Circe.decodeCaseInsensitive(this)
}
