package name.abhijitsarkar.scala

import java.io.File
import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ FileIO, Sink }
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestProbe
import name.abhijitsarkar.scala.WeatherService._
import org.scalatest.{ FlatSpec, Matchers }

import scala.concurrent.duration._
import scala.util.Failure

/**
 * @author Abhijit Sarkar
 */
class WeatherServiceSpec extends FlatSpec with Matchers {
  implicit val system = ActorSystem("weather")
  implicit val materializer = ActorMaterializer()
  // Only compiles if system = ActorSystem("weather") has been defined
  import system.dispatcher

  "weather service" should "find test csv files" in {
    val p = new File(getClass.getResource("/").toURI).getAbsolutePath

    val in = inFiles(p)

    in.size shouldBe 4
  }

  it should "emit single daily weather" in {
    val p = Paths.get(getClass.getResource("/test1.csv").toURI)

    val probe = TestProbe()
    FileIO.fromPath(p)
      .via(dailyWeathers)
      .map(_.stationId.map(_.id).getOrElse(""))
      .runWith(Sink.head)
      .pipeTo(probe.ref)

    probe.expectMsg(100.millis, "00058063")
  }

  it should "find summary results" in {
    val p = Paths.get(getClass.getResource("/test2.csv").toURI)

    val probe = TestProbe()
    val result = FileIO.fromPath(p)
      .via(dailyWeathers)
      .via(summary)
      .runWith(Sink.head)
      .pipeTo(probe.ref)

    probe.expectMsg(100.millis, (1859, 358.0))
  }

  it should "combine Nil path" in {
    val p = Nil
    val src = combine(p)

    src.runWith(Sink.head)
      .onComplete {
        _ match {
          case Failure(x) => x shouldBe a[NoSuchElementException]
          case _          => fail
        }
      }
  }

  it should "combine single path" in {
    val p = Paths.get(getClass.getResource("/test1.csv").toURI)
    val paths = List(p)
    val src = combine(paths)

    val probe = TestProbe()
    src.mapConcat(_.utf8String.split("\\R").toStream)
      .runWith(Sink.head)
      // Only compiles if implicit Execution context is present
      .pipeTo(probe.ref)

    probe.expectMsg(100.millis, "ASN00058063,18590101,PRCP,0,,,a,")
  }

  it should "combine two paths" in {
    val p1 = Paths.get(getClass.getResource("/test1.csv").toURI)
    val p2 = Paths.get(getClass.getResource("/test2.csv").toURI)
    val paths = List(p1, p2)
    val src = combine(paths)

    val results = src.mapConcat(_.utf8String.split("\\R").toStream)
      .runWith(TestSink.probe[String])
      .request(2)
      .expectNextN(2)

    results.size shouldBe 2
    results should contain allOf (
      "ASN00058063,18590101,PRCP,0,,,a,",
      "ASN00086071,18590101,TMAX,358,,,a,"
    )
  }

  it should "combine three paths" in {
    val p1 = Paths.get(getClass.getResource("/test1.csv").toURI)
    val p2 = Paths.get(getClass.getResource("/test2.csv").toURI)
    val p3 = Paths.get(getClass.getResource("/test3.csv").toURI)
    val paths = List(p1, p2, p3)
    val src = combine(paths)

    val results = src.mapConcat(_.utf8String.split("\\R").toStream)
      .runWith(TestSink.probe[String])
      .request(3)
      .expectNextN(3)

    results.size shouldBe 3
    results should contain allOf (
      "ASN00058063,18590101,PRCP,0,,,a,",
      "ASN00086071,18590101,TMAX,358,,,a,",
      "ASN00086071,18590101,TMIN,196,,,a,"
    )
  }
}
