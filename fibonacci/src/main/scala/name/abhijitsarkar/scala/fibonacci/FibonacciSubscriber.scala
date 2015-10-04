package name.abhijitsarkar.scala.fibonacci

import akka.actor.ActorLogging
import akka.stream.actor.ActorSubscriber
import akka.stream.actor.ActorSubscriberMessage.OnComplete
import akka.stream.actor.ActorSubscriberMessage.OnError
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.WatermarkRequestStrategy

class FibonacciSubscriber extends ActorSubscriber with ActorLogging {
  /**
   * If the number of unhandled messages is less than the low watermark (default is half of high watermark),
   * this strategy requests enough elements to meet the high watermark.
   */
  val requestStrategy = WatermarkRequestStrategy(20)

  def receive = {
    case OnNext(fib: Long) =>
      log.debug("Received Fibonacci number: {}.", fib)

      if (fib > 5000) self ! OnComplete
    case OnError(ex: Exception) =>
      log.error(ex, ex.getMessage)
      self ! OnComplete
    case OnComplete =>
      log.info("Stopping subscriber.")
      context.stop(self)
    case unknown => log.warning("Received unknown event: {}.", unknown)
  }
}