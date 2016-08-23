package name.abhijitsarkar.scala

/**
 * @author Abhijit Sarkar
 */
case class DailyWeather(
    stationId: Option[StationId],
    dateOfObservation: Option[DateOfObservation],
    elementType: ElementType,
    elementValue: String,
    measurementFlag: Option[MeasurementFlag],
    qualityFlag: Option[QualityFlag],
    sourceFlag: Option[SourceFlag],
    timeOfObservation: Option[TimeOfObservation]) {
  override def toString = s"$stationId,$dateOfObservation,$elementType,$elementValue," +
    s"${measurementFlag.get},${qualityFlag.get},${sourceFlag.get},${timeOfObservation.getOrElse("None")}"
}
