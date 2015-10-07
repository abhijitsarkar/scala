package name.abhijitsarkar.scala.meetup.model

import spray.json.DefaultJsonProtocol
import scala.util.Try
import scala.util.Success
import spray.json._
import org.slf4j.LoggerFactory
import scala.util.Failure
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

object RsvpJsonSupport extends DefaultJsonProtocol {
  implicit val venueFormat = jsonFormat(Venue, "venue_name")
  implicit val eventFormat = jsonFormat(Event, "event_name")
  implicit val memberFormat = jsonFormat(Member, "member_name")

  implicit val rsvpFormat = jsonFormat3(Rsvp)
}