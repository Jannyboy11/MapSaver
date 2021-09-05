package fr.epicanard.mapsaver.circe

import io.circe.generic.extras.Configuration

trait CapitalizeConfiguration {
  implicit val configuration: Configuration = Configuration.default.copy(transformMemberNames = _.capitalize)
}
