package name.abhijitsarkar.user

import akka.actor.ActorSystem
import akka.stream.Materializer
import scala.concurrent.ExecutionContextExecutor
import com.typesafe.config.Config
import akka.event.LoggingAdapter
import scala.concurrent.duration.DurationInt
import akka.util.Timeout
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.event.Logging

trait ActorPlumbing {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  implicit val timeout = Timeout(1 second)

  def config: Config
  val logger: LoggingAdapter
}