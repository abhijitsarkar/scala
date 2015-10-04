package name.abhijitsarkar.scala.scauth.service

import akka.actor.ActorLogging
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.ActorSubscriberMessage.OnComplete
import akka.stream.actor.ActorSubscriberMessage.OnError
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.WatermarkRequestStrategy
import name.abhijitsarkar.scala.scauth.model.Tweet

class TwitterSubscriber extends ActorSubscriber with ActorLogging {
  val requestStrategy = WatermarkRequestStrategy(10)

  def receive = {
    case OnNext(tweet: Tweet) =>
      log.debug("Received tweet: {}.", tweet)
    case OnError(ex: Exception) =>
      log.error(ex, ex.getMessage)
      context.stop(self)
    case OnComplete =>
      log.info(s"${getClass().getSimpleName} Completed!")
      context.stop(self)
    case unknown => log.warning("Unknown event: {}.", unknown)
  }
}