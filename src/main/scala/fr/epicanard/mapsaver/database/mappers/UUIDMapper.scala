package fr.epicanard.mapsaver.database.mappers

import slick.jdbc.{GetResult, SetParameter}

import java.util.UUID

object UUIDMapper {
  implicit val uuidSetParameter: SetParameter[UUID] = SetParameter[UUID]((v, pp) => pp.setString(v.toString))
  implicit val uuidGetResult: GetResult[UUID]       = GetResult[UUID](rs => UUID.fromString(rs.nextString()))
}
