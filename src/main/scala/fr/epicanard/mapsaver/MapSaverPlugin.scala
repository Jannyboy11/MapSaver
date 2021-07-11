package fr.epicanard.mapsaver

import xyz.janboerman.scalaloader.plugin.ScalaPlugin
import xyz.janboerman.scalaloader.plugin.ScalaPluginDescription
import xyz.janboerman.scalaloader.plugin.description.Scala
import xyz.janboerman.scalaloader.plugin.description.ScalaVersion
import fr.epicanard.mapsaver.resources.ResourceLoader
import fr.epicanard.mapsaver.models.config.Config
import fr.epicanard.mapsaver.models.config.Config._
import java.io.File
import buildinfo.BuildInfo
import io.circe.Decoder

@Scala(version = ScalaVersion.v2_13_6)
object MapSaverPlugin
    extends ScalaPlugin(
      new ScalaPluginDescription(BuildInfo.name, BuildInfo.version)
    ) {

  override def onEnable(): Unit = {
    val config: Option[Config] = loadResource("config.yml")
    print(config)
  }

  def loadResource[T](path: String)(implicit decoder: Decoder[T]): Option[T] = {
    if (!new File(getDataFolder, path).exists) saveResource(path, false)
    ResourceLoader.loadFromPath[T](s"$getDataFolder/$path")
  }

}
