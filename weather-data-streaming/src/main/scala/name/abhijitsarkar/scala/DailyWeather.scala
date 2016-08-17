package name.abhijitsarkar.scala

/**
  * @author Abhijit Sarkar
  */
case class DailyWeather(
                         stationId: StationId,
                         dateOfObservation: DateOfObservation,
                         elementType: ElementType,
                         elementValue: String,
                         measurementFlag: Option[MeasurementFlag],
                         qualityFlag: Option[QualityFlag],
                         sourceFlag: Option[SourceFlag],
                         timeOfObservation: Option[TimeOfObservation]
                       )
