package name.abhijitsarkar.scala

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Broadcast, Sink, Source }

import scala.concurrent.Await

/**
 * @author Abhijit Sarkar
 */
object CombineSinks extends App {
  val incrementSink = Sink.foreach[Int](x => println(s"increment: ${x + 1}"))
  val doubleSink = Sink.foreach[Int](x => println(s"double: ${x * 2}"))

  val sink = Sink.combine(incrementSink, doubleSink)(Broadcast[Int](_))

  implicit val system = ActorSystem("combine")

  implicit val materializer = ActorMaterializer()

  Source(List(1, 2, 3)).runWith(sink)

  val maybeTerminate = system.terminate()

  import scala.concurrent.duration._

  Await.result(maybeTerminate, 1.minute)
}
