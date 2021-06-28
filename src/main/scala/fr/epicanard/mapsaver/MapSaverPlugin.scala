package fr.epicanard.mapsaver

import xyz.janboerman.scalaloader.plugin.ScalaPlugin
import xyz.janboerman.scalaloader.plugin.ScalaPluginDescription
import xyz.janboerman.scalaloader.plugin.description.Scala
import xyz.janboerman.scalaloader.plugin.description.ScalaVersion
import fr.epicanard.mapsaver.resources.ResourceLoader
import fr.epicanard.mapsaver.resources.Config
import fr.epicanard.mapsaver.resources.Config.configDecoder
import fr.epicanard.mapsaver.resources.Decodable
import java.io.File
import buildinfo.BuildInfo

@Scala(ScalaVersion.v3_0_0)
object MapSaverPlugin
    extends ScalaPlugin(ScalaPluginDescription(BuildInfo.name, BuildInfo.version)):

  override def onEnable(): Unit =
    val config: Option[Config] = loadResource("config.yml")
    print(config)

  def loadResource[T](path: String): Decodable[Option, T] =
    if !new File(getDataFolder, path).exists then
      saveResource(path, false)
    ResourceLoader.loadFromPath(s"$getDataFolder/$path")
