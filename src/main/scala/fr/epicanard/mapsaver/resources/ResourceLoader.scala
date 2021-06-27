package fr.epicanard.mapsaver.resources

import io.circe.yaml.parser
import io.circe.generic.auto._
import io.circe.Decoder
import java.io.InputStreamReader
import java.io.File

import scala.util.Try
import scala.io.Source
import scala.util.Failure

import fr.epicanard.mapsaver.MapSaverPlugin

object ResourceLoader:

  def loadFromPath[T](path: String): Decodable[Option, T] =
    (for
      content   <- readFile(path)
      resource  <- parseContent(content)
    yield resource)
      .recoverWith(handleErrors(path))
      .toOption

  private def parseContent[T](content: String): Decodable[Try, T] =
    for
      json      <- parser.parse(content).toTry
      resource  <- json.as[T].toTry
    yield resource

  private def readFile(path: String): Try[String] =
    for
      file    <- Try(Source.fromFile(path))
      content = file.getLines.mkString("\n")
      _       <- Try(file.close)
    yield content

  private def handleErrors[T](path: String): PartialFunction[Throwable, Try[T]] =
    case e =>
      MapSaverPlugin.getLogger.warning(s"Can't load file: $path")
      e.printStackTrace()
      Failure(e)

