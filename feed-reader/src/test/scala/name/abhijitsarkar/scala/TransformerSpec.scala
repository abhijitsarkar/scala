package name.abhijitsarkar.scala

import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Abhijit Sarkar
  */
class TransformerSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll {
  def this() = this(ActorSystem("TransformerSpec"))

  implicit val materializer = ActorMaterializer()

  /* Use the system's dispatcher as ExecutionContext */
  import system.dispatcher

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  /* https://github.com/matyjas/testing-akka/blob/master/src/test/scala/events/EventActorSpec.scala */
  "Transformer" should "extract temperature" in {
    val path = NoaaClient.currentConditionsPath(false)
    val files = Files.newDirectoryStream(Paths.get(path), "*.xml")

    val transformer = system.actorOf(Transformer.props, "transformer")

    system.scheduler.scheduleOnce(50 milliseconds, transformer, Message(files, "temp_f"))

    within(1 second) {
      val flow = expectMsgClass(classOf[Source[(String, Seq[String]), Unit]])

      val future = flow.runWith(Sink.foreach(e => println(s"${e._1} -> ${e._2}")))

      Await.result(future, 10 seconds)
    }

    files.close
  }
}
