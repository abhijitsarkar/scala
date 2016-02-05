package name.abhijitsarkar.scala

import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.collection.immutable.{List => ImmutableList}
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Abhijit Sarkar
  */
object Transformer {
  implicit val system = ActorSystem("transformer")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = {
    implicitly
  }

  import collection.JavaConverters._

  def run(path: String, text: String, fileFilter: String) = {
    Source.fromIterator { () =>
      Files.newDirectoryStream(Paths.get(path), fileFilter).iterator().asScala
    }.map(p => {
      val lines = io.Source.fromFile(p.toFile).getLines().filter(_.contains(text)).map(_.trim).to[ImmutableList]
      (p, lines)
    })
      .runWith(Sink.foreach(e => println(s"${e._1} -> ${e._2}")))
  }
}
