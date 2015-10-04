package name.abhijitsarkar.scala.scauth.service

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import org.slf4j.LoggerFactory
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing
import name.abhijitsarkar.scala.scauth.api.OAuthRequest
import name.abhijitsarkar.scala.scauth.api.OAuthService

class SimpleOAuthService[A]()(implicit val actorPlumbing: ActorPlumbing) extends OAuthService[A] {
  private val log = LoggerFactory.getLogger(getClass())

  protected def sendAndReceive(httpRequest: HttpRequest, id: String)(implicit unmarshaller: Unmarshaller[ResponseEntity, A]) = {
    log.debug(s"Http request: {}.", httpRequest)

    import actorPlumbing._

    val pool = Http().superPool[String]()

    val response = Source.single(httpRequest -> id)
      .via(pool)
      .runWith(Sink.head)

    response.flatMap { r =>
      r._1 match {
        case Success(results) => Unmarshal(results.entity).to[A]
        case Failure(ex) => Future.failed(ex)
      }
    }
  }
}