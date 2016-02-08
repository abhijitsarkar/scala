package name.abhijitsarkar.scala

import java.nio.file.{DirectoryStream, Path}

import akka.actor.{Actor, Props}
import akka.stream.scaladsl.Source

import scala.collection.immutable.{List => ImmutableList}

/**
  * @author Abhijit Sarkar
  */

case class Message(dir: DirectoryStream[Path], text: String)

import scala.collection.JavaConverters._

class Transformer extends Actor {
  def receive = {
    case Message(dir, text) => {
      val flow = Source(dir.asScala.toList).map(p => {
        val lines = io.Source.fromFile(p.toFile).getLines().filter(_.contains(text)).map(_.trim).to[ImmutableList]
        (p.toAbsolutePath.toString, lines)
      }).filter(!_._2.isEmpty)

      sender ! flow
    }
  }
}

object Transformer {
  def props = Props(new Transformer)
}
