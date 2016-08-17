package name.abhijitsarkar.scala

import java.nio.charset.StandardCharsets.UTF_8

import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Abhijit Sarkar
  */
class DailyWeatherSpec extends FlatSpec with Matchers {
  "daily weather" should "parse weather file" in {
    io.Source.fromInputStream(getClass.getResourceAsStream("/1859.csv"), UTF_8.name())
      .getLines()
      .map(_.split(",").map(_.trim))
      .map { arr =>
        val stationId: StationId = arr(0) match {
          case StationId(countryCode, networkCode, id) => StationId(countryCode, networkCode, id)
        }

        val dateOfObservation: DateOfObservation = arr(1) match {
          case DateOfObservation(year, month, date) => DateOfObservation(year, month, date)
        }

        val elementType: ElementType = ElementType.values.find(x => arr(2).matches(x.code)).get

        val elementValue = arr(3)

        val measurementFlag = MeasurementFlag.values.find(_.code == arr(4))

        val qualityFlag = QualityFlag.values.find(_.code == arr(5))

        val sourceFlag = arr.lift(6).map(SourceFlag)

        val timeOfObservation: Option[TimeOfObservation] = arr.lift(7) match {
          case Some(x) => x match {
            case TimeOfObservation(hour, min) => Some(TimeOfObservation(hour, min))
          }
          case _ => None
        }

        DailyWeather(stationId, dateOfObservation, elementType, elementValue, measurementFlag, qualityFlag, sourceFlag, timeOfObservation)
      }
      .foreach(println)
  }
}
