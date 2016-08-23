package name.abhijitsarkar.scala

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{ FlatSpec, Matchers }

import scala.concurrent.{ Await, Future }

/**
 * @author Abhijit Sarkar
 */
class PartialGraphSpec extends FlatSpec with Matchers {
  "partial graph" should "pick the greatest of 3 values" in {
    import PartialGraph._
    implicit val system = ActorSystem("partial")

    implicit val materializer = ActorMaterializer()

    val max: Future[Int] = g.run

    import scala.concurrent.duration._
    Await.result(max, 300.millis) should equal(3)
  }
}
