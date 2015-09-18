package name.abhijitsarkar.scala

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import akka.util.Timeout

trait ActorPlumbing {
  implicit val executionContext = global
  implicit val timeOut = Timeout(3 seconds)
  implicit val duration = timeOut.duration
}