package fr.epicanard.mapsaver.models

import org.bukkit.OfflinePlayer

case class Pageable(player: OfflinePlayer, page: Int, maxPage: Int = 1, pageSize: Int = 10)
