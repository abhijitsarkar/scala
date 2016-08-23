package name.abhijitsarkar.scala

import java.nio.charset.StandardCharsets.UTF_8

import org.scalatest.{ FlatSpec, Matchers }

/**
 * @author Abhijit Sarkar
 */
class DailyWeatherSpec extends FlatSpec with Matchers {
  "daily weather" should "parse weather file" in {
    val w = io.Source.fromInputStream(getClass.getResourceAsStream("/test1.csv"), UTF_8.name())
      .getLines()
      .map(_.split(",").map(_.trim))
      .map { arr =>
        val stationId = StationId(arr(0))

        val dateOfObservation = DateOfObservation(arr(1))

        val elementType = ElementType(arr(2))

        val elementValue = arr(3)

        val measurementFlag = arr.lift(4).map(MeasurementFlag(_))

        val qualityFlag = arr.lift(5).map(QualityFlag(_))

        val sourceFlag = arr.lift(6).map(SourceFlag(_))

        val timeOfObservation = arr.lift(7).map(TimeOfObservation(_))

        DailyWeather(stationId, dateOfObservation, elementType, elementValue,
          measurementFlag, qualityFlag, sourceFlag, timeOfObservation)
      }
      .toList
      .head

    w.stationId.map(_.id).getOrElse("") shouldBe "00058063"
  }
}
