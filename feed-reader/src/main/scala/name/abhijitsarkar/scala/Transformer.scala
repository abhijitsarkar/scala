package name.abhijitsarkar.scala

import java.nio.file.{Files, Path, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Abhijit Sarkar
  */
object Transformer {
  implicit val system = ActorSystem("twitter")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = {
    implicitly
  }

  import collection.JavaConverters._

  def run = {
    val flow = Source[Path] {
      val url = NoaaClient.currentConditionsUrl()
      Files.newDirectoryStream(Paths.get(url), "*.xml").asScala.to[collection.immutable.List]
    }.map(p => io.Source.fromFile(p.toFile).getLines().filter(_.contains("temp_f")).mkString)
      .runWith(Sink.foreach(println(_)))
  }
}
