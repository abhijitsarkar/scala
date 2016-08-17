package name.abhijitsarkar.scala

/**
  * ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/daily/readme.txt
  *
  * @author Abhijit Sarkar
  *
  */
class StationId(val countryCode: String, val networkCode: String, val id: String) {
  override def toString = s"StationId(countryCode = $countryCode, networkCode = $networkCode, id = $id)"
}

object StationId {
  val p = """(..)(.)(.{8})""".r

  def apply(countryCode: String, networkCode: String, id: String) = new StationId(countryCode, networkCode, id)

  def unapply(arg: String): Option[(String, String, String)] = {
    arg match {
      case p(countryCode, networkCode, id) => Some((countryCode, networkCode, id))
      case _ => None
    }
  }
}

class DateOfObservation(val year: Int, val month: Int, val date: Int) {
  override def toString = s"DateOfObservation(year = $year, month = $month, date = $date)"
}

object DateOfObservation {
  val p = """(\d{4})(\d{2})(\d{2})""".r

  def apply(year: Int, month: Int, date: Int) = new DateOfObservation(year, month, date)

  def unapply(arg: String): Option[(Int, Int, Int)] = {
    arg match {
      case p(year, month, date) => Some((year.toInt, month.toInt, date.toInt))
      case _ => None
    }
  }
}

case class SourceFlag(code: String)

class TimeOfObservation(val hour: Int, val min: Int) {
  override def toString = s"TimeOfObservation(hour = $hour, min = $min)"
}

object TimeOfObservation {
  val p = """(\d{2})(\d{2})""".r

  def apply(hour: Int, min: Int) = new TimeOfObservation(hour, min)

  def unapply(arg: String): Option[(Int, Int)] = {
    arg match {
      case p(hour, min) => Some((hour.toInt, min.toInt))
      case _ => None
    }
  }
}


