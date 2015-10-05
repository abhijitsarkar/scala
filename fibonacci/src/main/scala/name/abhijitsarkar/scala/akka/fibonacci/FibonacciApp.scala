package name.abhijitsarkar.scala.akka.fibonacci

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.FlowGraph
import akka.stream.scaladsl.Broadcast
import akka.stream.SinkShape
import akka.stream.scaladsl.Keep

object FibonacciApp extends App {
  implicit val system = ActorSystem("fibonacci")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = { implicitly }

  // build graph
  val flow = Flow[Long]
  val evenSubscriber = Sink.actorSubscriber(FibonacciSubscriber.props("even"))
  val oddSubscriber = Sink.actorSubscriber(FibonacciSubscriber.props("odd"))

  val evenSink = flow.filter { _ % 2 == 0 }.toMat(evenSubscriber)(Keep.left)
  val oddSink = flow.filter { _ % 2 != 0 }.toMat(oddSubscriber)(Keep.left)

  val partial = FlowGraph.partial() { implicit builder =>
    import FlowGraph.Implicits._

    val broadcast = builder.add(Broadcast[Long](2))

    broadcast ~> evenSink
    broadcast ~> oddSink

    SinkShape(broadcast.in)
  }

  // run graph
  val src = Source.actorPublisher(Props[FibonacciPublisher])
  src.runWith(partial)
}