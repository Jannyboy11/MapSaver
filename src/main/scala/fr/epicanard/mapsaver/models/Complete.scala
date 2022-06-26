package fr.epicanard.mapsaver.models

import fr.epicanard.mapsaver.errors.Error

import scala.concurrent.Future

trait Complete {
  this: Complete =>
  def fsuccess: Future[Either[Error, Complete]] = Future.successful(Right(this))
}

object Complete {
  case class Custom(results: List[String]) extends Complete
  case object Players                      extends Complete
  case object Empty                        extends Complete

  def getResults(complete: Complete): List[String] = complete match {
    case Custom(results) => results
    case Players         => null // If null spigot auto handle autocompletion as a list of players
    case Empty           => Nil
  }
}
