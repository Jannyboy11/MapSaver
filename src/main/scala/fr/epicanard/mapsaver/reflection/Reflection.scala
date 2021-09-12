package fr.epicanard.mapsaver.reflection

import cats.syntax.either._
import fr.epicanard.mapsaver.errors.TechnicalError
import fr.epicanard.mapsaver.errors.TechnicalError.ReflectionError

import java.lang.reflect.Field
import scala.util.Try

object Reflection {

  def getFieldByType[T](obj: Any, typeName: String): Either[TechnicalError, T] =
    getMemberFromField[T](
      obj,
      obj.getClass.getDeclaredFields
        .find(_.getType.getCanonicalName == typeName)
        .getOrElse(throw new NoSuchFieldException(s"Type => $typeName"))
    )

  def getFieldByName[T](obj: Any, fieldName: String): Either[TechnicalError, T] =
    getMemberFromField[T](obj, obj.getClass.getDeclaredField(fieldName))

  private def getMemberFromField[T](obj: Any, getField: => Field): Either[TechnicalError, T] =
    Try {
      val field = getField
      field.setAccessible(true)
      field.get(obj).asInstanceOf[T]
    }.toEither.leftMap(ReflectionError)
}
