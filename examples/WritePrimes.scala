package name.abhijitsarkar.scala.akka.example

import java.io.File
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }
import akka.stream.io.Implicits.AddSynchronousFileSink
import akka.stream.scaladsl.FlowGraph.Implicits.SourceArrow
import akka.stream.scaladsl.FlowGraph.Implicits.fanOut2flow
import scala.concurrent.Future
import akka.stream.UniformFanOutShape

// Taken from https://github.com/typesafehub/activator-akka-stream-scala/blob/master/src/main/scala/sample/stream/WritePrimes.scala
// and tweaked to aid understanding
object WritePrimes {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("Sys")
    import system.dispatcher
    implicit val materializer = ActorMaterializer()

    // generate random numbers
    val maxRandomNumberSize = 1000000
    val primeSource: Source[Int, Unit] =
      Source(() => Iterator.continually(ThreadLocalRandom.current().nextInt(maxRandomNumberSize))).
        // filter prime numbers
        filter(rnd => isPrime(rnd)).
        // and neighbor +2 is also prime
        filter(prime => isPrime(prime + 2))

    import akka.stream.io.Implicits._

    // write to file sink
    val fileSink: Sink[ByteString, Future[Long]] = Sink.synchronousFile(new File("target/primes.txt"))
    val flow: Flow[Int, ByteString, Unit] = Flow[Int]
      // act as if processing is really slow
      .map(i => { Thread.sleep(1000); ByteString(i.toString) })
    val slowSink: Sink[Int, Future[Long]] = flow.toMat(fileSink)((_, bytesWritten) => bytesWritten)

    // console output sink
    val consoleSink: Sink[Int, Future[Unit]] = Sink.foreach[Int](println)

    // send primes to both slow file sink and console sink using graph API
    val materialized = FlowGraph.closed(slowSink, consoleSink)((slow: Future[Long], console: Future[Unit]) => slow) {
      implicit builder =>
        (s, c) =>
          import FlowGraph.Implicits._

          val broadcast: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2)) // the splitter - like a Unix tee
          primeSource ~> broadcast ~> s // connect primes to splitter, and one side to file
          broadcast ~> c // connect other side of splitter to console
    }.run()

    // ensure the output file is closed and the system shutdown upon completion
    materialized.onComplete {
      case Success(_) =>
        system.shutdown()
      case Failure(e) =>
        println(s"Failure: ${e.getMessage}")
        system.shutdown()
    }
  }

  def isPrime(n: Int): Boolean = {
    if (n <= 1) false
    else if (n == 2) true
    else !(2 to (n - 1)).exists(x => n % x == 0)
  }
}