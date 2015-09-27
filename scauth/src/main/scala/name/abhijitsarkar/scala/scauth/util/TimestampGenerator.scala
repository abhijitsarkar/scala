package name.abhijitsarkar.scala.scauth.util

trait TimestampGenerator {
  def generateTimestampInSeconds: String
}

object SimpleTimestampGenerator extends TimestampGenerator {
  override def generateTimestampInSeconds = {
    String.valueOf(System.currentTimeMillis() / 1000)
  }
}