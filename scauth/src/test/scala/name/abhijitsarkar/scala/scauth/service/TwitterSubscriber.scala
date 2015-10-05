package name.abhijitsarkar.scala.scauth.service

import org.slf4j.LoggerFactory
import akka.actor.actorRef2Scala
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.ActorSubscriberMessage.OnComplete
import akka.stream.actor.ActorSubscriberMessage.OnError
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.WatermarkRequestStrategy
import name.abhijitsarkar.scala.scauth.model.Tweet
import akka.actor.Props

class TwitterSubscriber(name: String) extends ActorSubscriber {
  private val log = LoggerFactory.getLogger(name)

  val requestStrategy = WatermarkRequestStrategy(10)

  def receive = {
    case OnNext(tweet: Tweet) =>
      log.debug("Received tweet: {}.", tweet)
    case OnError(ex: Exception) =>
      log.error(ex.getMessage, ex)
      self ! OnComplete
    case OnComplete =>
      log.info(s"$name completed.")
      context.stop(self)
    case unknown => log.warn("Unknown event: {}.", unknown)
  }
}

object TwitterSubscriber {
  def props(name: String) = Props(new TwitterSubscriber(name))
}