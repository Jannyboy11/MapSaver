package fr.epicanard.mapsaver.database.queries

import fr.epicanard.mapsaver.database.SQLActionBuilderExt._
import fr.epicanard.mapsaver.database.mappers.PlayerMapMapper._
import fr.epicanard.mapsaver.database.mappers.UUIDMapper._
import fr.epicanard.mapsaver.database.mappers.VisibilityMapper._
import fr.epicanard.mapsaver.database.profile.MySQLProfile.api._
import fr.epicanard.mapsaver.database.schema.PlayerMaps
import fr.epicanard.mapsaver.models.Pageable
import fr.epicanard.mapsaver.models.map.{PlayerMap, Visibility}

import java.util.UUID

object PlayerMapQueries {

  def selectByDataId(dataId: Int): DBIO[Option[PlayerMap]] =
    PlayerMaps.filter(m => m.dataId === dataId).take(1).result.headOption

  def insert(playerMap: PlayerMap): DBIO[Int] = PlayerMaps += playerMap

  def countForPlayer(playerUUID: UUID, restrictVisibility: Option[Visibility]): DBIO[Int] =
    (sql"""
      SELECT count(*) FROM player_maps 
      WHERE `player_uuid` = $playerUUID
      """
      +? restrictVisibility.map(vis => sql" AND `visibility` = $vis ")).as[Int].head

  def listForPlayer(
      pageable: Pageable,
      restrictVisibility: Option[Visibility]
  ): DBIO[Seq[PlayerMap]] = {
    val start = (pageable.page - 1) * pageable.pageSize
    (sql"""
      SELECT p.* FROM player_maps AS p
      LEFT JOIN data_maps ON p.data_id = data_maps.id
      WHERE `player_uuid` = ${pageable.player.getUniqueId}
      """
      +? restrictVisibility.map(vis => sql" AND `visibility` = $vis ")
      ++ sql"""
      ORDER BY data_maps.updated_at DESC
      LIMIT $start,${pageable.pageSize}
      """).as[PlayerMap]
  }

  def selectPlayerMap(
      owner: UUID,
      dataId: Int,
      restrictVisibility: Option[Visibility]
  ): DBIO[Option[PlayerMap]] =
    (sql"""SELECT * FROM player_maps WHERE data_id = $dataId"""
      +? restrictVisibility.map(vis => sql" AND (player_uuid = $owner OR`visibility` = $vis) "))
      .as[PlayerMap]
      .headOption

  def selectPlayerMapWithName(
      owner: UUID,
      mapName: String,
      restrictVisibility: Option[Visibility]
  ): DBIO[Option[PlayerMap]] =
    (sql"""SELECT * FROM player_maps WHERE `player_uuid` = $owner AND name = $mapName"""
      +? restrictVisibility.map(vis => sql" AND `visibility` = $vis ")).as[PlayerMap].headOption

}
