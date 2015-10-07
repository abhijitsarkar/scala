package name.abhijitsarkar.scala.meetup.service

import org.slf4j.LoggerFactory
import akka.actor.actorRef2Scala
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.ActorSubscriberMessage.OnComplete
import akka.stream.actor.ActorSubscriberMessage.OnError
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.WatermarkRequestStrategy
import akka.actor.Props
import name.abhijitsarkar.scala.meetup.model.Rsvp

class RsvpSubscriber(name: String) extends ActorSubscriber {
  private val log = LoggerFactory.getLogger(name)

  val requestStrategy = WatermarkRequestStrategy(10)

  def receive = {
    case OnNext(rsvp: Rsvp) =>
      log.debug("Received rsvp: {}.", rsvp)
    case OnError(ex: Exception) =>
      log.error(ex.getMessage, ex)
      self ! OnComplete
    case OnComplete =>
      log.info(s"$name completed.")
      context.stop(self)
    case unknown => log.warn("Unknown event: {}.", unknown)
  }
}

object RsvpSubscriber {
  def props(name: String) = Props(new RsvpSubscriber(name))
}