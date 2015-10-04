package name.abhijitsarkar.scala.fibonacci

import scala.collection.mutable.Queue

import akka.actor.ActorLogging
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Cancel
import akka.stream.actor.ActorPublisherMessage.Request

class FibonacciPublisher extends ActorPublisher[Long] with ActorLogging {
  private val queue = Queue[Long](0, 1)

  def receive = {
    case Request(_) => // _ is the demand
      log.debug("Received request; demand = {}.", totalDemand)
      publish
    case Cancel =>
      log.info("Stopping publisher.")
      context.stop(self)
    case unknown => log.warning("Received unknown event: {}.", unknown)
  }

  final def publish = {
    while (isActive && totalDemand > 0) {
      val next = queue.head
      queue += (queue.dequeue + queue.head)

      log.debug("Producing fibonacci number: {}.", next)

      onNext(next)

      if (next > 5000) self ! Cancel
    }
  }
}