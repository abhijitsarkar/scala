package name.abhijitsarkar.user

import akka.actor.ActorSystem
import akka.stream.Materializer
import scala.concurrent.ExecutionContextExecutor
import com.typesafe.config.Config
import akka.event.LoggingAdapter

trait ActorPlumbing {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter
}