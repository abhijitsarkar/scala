package name.abhijitsarkar.scala.meetup

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.SinkShape
import akka.stream.scaladsl.Broadcast
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.FlowGraph
import akka.stream.scaladsl.FlowGraph.Implicits.fanOut2flow
import akka.stream.scaladsl.Sink
import name.abhijitsarkar.scala.meetup.model.Rsvp
import name.abhijitsarkar.scala.meetup.model.RsvpJsonSupport.rsvpFormat
import name.abhijitsarkar.scala.meetup.service.MeetupStreamingService
import name.abhijitsarkar.scala.meetup.service.RsvpSubscriber
import name.abhijitsarkar.scala.meetup.util.ActorPlumbing
import spray.json.pimpString
import akka.util.ByteString
import akka.stream.Graph

object MeetupStreamingApp extends App {
  implicit val system = ActorSystem("twitter")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = { implicitly }

  implicit val actorPlumbing: ActorPlumbing = ActorPlumbing()

  val firstSubscriber = Sink.actorSubscriber(RsvpSubscriber.props("first"))
  val secondSubscriber = Sink.actorSubscriber(RsvpSubscriber.props("second"))

  val rsvpFlow: Flow[ByteString, Rsvp, Unit] = Flow[ByteString].map {
    import name.abhijitsarkar.scala.meetup.model.RsvpJsonSupport._
    import spray.json._

    _.utf8String.parseJson.convertTo[Rsvp]
  }

  val rsvpSink: Graph[SinkShape[ByteString], Unit] = FlowGraph.partial() { implicit builder =>
    import FlowGraph.Implicits._
    val broadcast = builder.add(Broadcast[Rsvp](2))

    val rsvp = builder.add(rsvpFlow)

    broadcast ~> firstSubscriber
    broadcast ~> secondSubscriber

    rsvp ~> broadcast

    SinkShape(rsvp.inlet)
  }

  val meetupStreamingService = new MeetupStreamingService(rsvpSink)

  meetupStreamingService.stream
}