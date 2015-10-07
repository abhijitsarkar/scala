package name.abhijitsarkar.scala.scauth.service

import scala.concurrent.Future
import org.slf4j.LoggerFactory
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import name.abhijitsarkar.scala.scauth.model.OAuthCredentials
import name.abhijitsarkar.scala.scauth.model.OAuthRequestConfig
import name.abhijitsarkar.scala.scauth.model.SimpleOAuthRequest
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing
import akka.stream.Graph
import akka.stream.SinkShape
import akka.stream.scaladsl.FlattenStrategy
import akka.util.ByteString

class TwitterStreamingService[T](val oAuthCredentials: OAuthCredentials,
    val partial: Graph[SinkShape[ByteString], Unit])(implicit val actorPlumbing: ActorPlumbing) {
  private val log = LoggerFactory.getLogger(getClass())

  private val baseUri = "https://stream.twitter.com/1.1"
  private val streamingUri = s"${baseUri}/statuses/filter.json"

  import actorPlumbing._

  def stream(follow: Option[String], track: Option[String]) = {
    val httpRequest = this.httpRequest { queryParams(follow, track) }
    val flow = this.flow { httpRequest }

    val src = Source.single(httpRequest).via(flow).map { _.entity.dataBytes }.flatten(FlattenStrategy.concat)

    src.runWith(partial)
  }

  private def queryParams(follow: Option[String], track: Option[String]) = {
    (follow, track) match {
      case (Some(a), Some(b)) => Map("follow" -> a, "track" -> b)
      case (Some(a), None) => Map("follow" -> a)
      case (None, Some(b)) => Map("track" -> b)
      case _ => throw new IllegalArgumentException("One of 'follow' and 'track' parameters must be specified.")
    }
  }

  private def httpRequest(queryParams: Map[String, String]) = {
    val oAuthRequestConfig = OAuthRequestConfig(baseUrl = streamingUri, queryParams = queryParams)
    val request = SimpleOAuthRequest(oAuthCredentials, oAuthRequestConfig)
    val httpRequest = request.toHttpRequestWithAuthorizationQueryParams

    log.debug(s"Http request: {}.", httpRequest)

    httpRequest
  }

  def flow(httpRequest: HttpRequest): Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]] = {
    val host = httpRequest.uri.authority.host.address()
    Http().outgoingConnectionTls(host)
  }
}