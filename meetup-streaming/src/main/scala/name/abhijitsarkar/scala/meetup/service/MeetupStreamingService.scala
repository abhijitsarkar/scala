package name.abhijitsarkar.scala.meetup.service

import org.slf4j.LoggerFactory

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri.apply
import akka.stream.Graph
import akka.stream.SinkShape
import akka.stream.scaladsl.FlattenStrategy
import akka.stream.scaladsl.Source
import akka.util.ByteString
import name.abhijitsarkar.scala.meetup.util.ActorPlumbing

class MeetupStreamingService(val sink: Graph[SinkShape[ByteString], Unit])(implicit val actorPlumbing: ActorPlumbing) {
  private val log = LoggerFactory.getLogger(getClass())

  private val baseUri = "http://stream.meetup.com/2/rsvps"

  import actorPlumbing._

  def stream = {
    val httpRequest = HttpRequest(uri = baseUri, method = GET)
    
    val flow = {
      val host = httpRequest.uri.authority.host.address()
      Http().outgoingConnectionTls(host)
    }

    val src: Source[ByteString, Unit] = Source.single(httpRequest).via(flow).map {
      _.entity.dataBytes
    }.flatten(FlattenStrategy.concat)

    src.runWith(sink)
  }
}