package fr.epicanard.mapsaver.models.circe

import enumeratum.Circe
import enumeratum.Enum
import enumeratum.EnumEntry
import io.circe.Decoder
import io.circe.Encoder

trait CirceEnumInsensitive[A <: EnumEntry] { this: Enum[A] =>
  implicit val circeEncoder: Encoder[A] = Circe.encoder(this)

  implicit val circeDecoder: Decoder[A] = Circe.decodeCaseInsensitive(this)
}
