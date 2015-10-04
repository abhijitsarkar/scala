package name.abhijitsarkar.scala.fibonacci

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

object FibonacciApp extends App {
  implicit val system = ActorSystem("fibonacci")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = { implicitly }

  val src = Source.actorPublisher(Props[FibonacciPublisher])
  val flow = Flow[Long].map { _ * 2 }
  val sink = Sink.actorSubscriber(Props[FibonacciSubscriber])

  src.via(flow).runWith(sink)
}