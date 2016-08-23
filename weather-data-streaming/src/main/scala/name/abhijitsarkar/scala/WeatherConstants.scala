package name.abhijitsarkar.scala

import java.io.File

import scala.util.Random

/**
 * ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/daily/readme.txt
 *
 * @author Abhijit Sarkar
 *
 */

sealed class StationId(val countryCode: String, val networkCode: String, val id: String) {
  override def toString = s"StationId($countryCode$networkCode$id)"
}

object StationId {
  val p = """(..)(.)(.{8})""".r

  def apply(arg: String) = {
    arg match {
      case p(countryCode, networkCode, id) => Some(new StationId(countryCode, networkCode, id))
      case x                               => println(s"Failed to parse $x into StationId."); None
    }
  }
}

sealed class DateOfObservation(val year: Int, val month: Int, val date: Int) {
  override def toString = s"DateOfObservation($year-$month-$date)"
}

object DateOfObservation {
  val p = """(\d{4})(\d{2})(\d{2})""".r

  def apply(arg: String) = {
    arg match {
      case p(year, month, date) => Some(new DateOfObservation(year.toInt, month.toInt, date.toInt))
      case x                    => println(s"Failed to parse $x into DateOfObservation."); None
    }
  }
}

object WeatherConstants {
  def toStringWithCode(className: String)(code: String) = s"$className${if (!code.isEmpty) "(" + code + ")" else ""}"

  def randomDirname = Random.alphanumeric.take(8).mkString

  def inDir = s"${System.getProperty("java.io.tmpdir")}${File.separator}weather${File.separator}$randomDirname"
}

sealed class ElementType(val code: String) {
  override def toString = s"${getClass.getSimpleName}"
}

object ElementType {
  def apply(code: String) = elementTypes.find(_.code == code).getOrElse(new ElementType(code))

  class Precipitation extends ElementType("PRCP")

  class SnowfallMillis extends ElementType("SNOW")

  class SnowDepthMillis extends ElementType("SNWD")

  class MaxTemp extends ElementType("TMAX")

  class MinTemp extends ElementType("TMIN")

  val maxTemp = new MaxTemp

  val elementTypes = List(new Precipitation, new SnowfallMillis, new SnowDepthMillis, maxTemp, new MinTemp)
}

sealed class MeasurementFlag(val code: String) {
  override def toString = s"${getClass.getSimpleName}"
}

import WeatherConstants._
object MeasurementFlag {
  def apply(code: String) = measurementFlags.find(_.code == code).getOrElse(new Other(code))

  class PrecipitationTotal12Hr extends MeasurementFlag("B")

  class PrecipitationTotal6Hr extends MeasurementFlag("D")

  class HourlyTemp extends MeasurementFlag("H")

  class Knots extends MeasurementFlag("K")

  class Lagged extends MeasurementFlag("L")

  class Okatas extends MeasurementFlag("O")

  class MissingPresumedZero extends MeasurementFlag("P")

  class Trace extends MeasurementFlag("T")

  class WindDirection extends MeasurementFlag("W")

  class Other(code: String) extends MeasurementFlag(code) {
    override def toString = if (!code.isEmpty) toStringWithCode("MeasurementFlag")(code) else "None"
  }

  val measurementFlags = List(new PrecipitationTotal12Hr, new PrecipitationTotal6Hr, new HourlyTemp,
    new Knots, new Lagged, new Okatas, new MissingPresumedZero, new Trace, new WindDirection)
}

sealed class QualityFlag(val code: String) {
  override def toString = toStringWithCode(getClass.getSimpleName)(code)
}

object QualityFlag {
  def apply(code: String) = if (code.isEmpty) good else new QualityFlag(code)

  class Good extends QualityFlag("") {
    override def toString = s"${getClass.getSimpleName}"
  }

  val good = new Good
}

sealed class SourceFlag(code: String) {
  override def toString = toStringWithCode(getClass.getSimpleName)(code)
}

object SourceFlag {
  def apply(code: String) = new SourceFlag(code)
}

sealed class TimeOfObservation(val hour: Int, val min: Int) {
  override def toString = s"TimeOfObservation($hour:$min)"
}

object TimeOfObservation {
  val p = """(\d{2})(\d{2})""".r

  def apply(arg: String) = {
    arg match {
      case p(hour, min) => new TimeOfObservation(hour.toInt, min.toInt)
      case _            => null
    }
  }
}

