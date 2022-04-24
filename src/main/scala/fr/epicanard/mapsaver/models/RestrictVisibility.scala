package fr.epicanard.mapsaver.models

import fr.epicanard.mapsaver.Permission
import fr.epicanard.mapsaver.models.map.Visibility
import org.bukkit.entity.Player

import java.util.UUID

sealed trait RestrictVisibility

object RestrictVisibility {

  case object Public        extends RestrictVisibility
  case object OwnerOrPublic extends RestrictVisibility
  case object OwnerOnly     extends RestrictVisibility

  def unless(permission: Permission, player: Player)(restriction: RestrictVisibility): Option[RestrictVisibility] =
    Option.unless(permission.isSetOn(player))(restriction)
}
