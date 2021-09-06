package fr.epicanard.mapsaver.resources

import io.circe.yaml.parser
import io.circe.Decoder

import scala.util.Try
import scala.io.Source
import cats.syntax.either._
import java.io.File

import fr.epicanard.mapsaver.errors.TechnicalError
import fr.epicanard.mapsaver.errors.TechnicalError._
import xyz.janboerman.scalaloader.plugin.ScalaPlugin

object ResourceLoader {

  def extractAndLoadResource[T](plugin: ScalaPlugin, path: String)(implicit
      decoder: Decoder[T]
  ): Either[TechnicalError, T] = {
    if (!new File(plugin.getDataFolder(), path).exists) plugin.saveResource(path, false)
    loadFromPath[T](s"${plugin.getDataFolder}/$path")
  }

  def loadFromPath[T](path: String)(implicit decoder: Decoder[T]): Either[TechnicalError, T] =
    (for {
      content  <- readFile(path)
      resource <- parseContent(content)
    } yield resource).toEither
      .leftMap(LoadConfigError(path, _))

  private def parseContent[T](
      content: String
  )(implicit decoder: Decoder[T]): Try[T] =
    for {
      json     <- parser.parse(content).toTry
      resource <- json.as[T].toTry
    } yield resource

  private def readFile(path: String): Try[String] =
    for {
      file <- Try(Source.fromFile(path))
      content = file.getLines().mkString("\n")
      _ <- Try(file.close)
    } yield content
}
