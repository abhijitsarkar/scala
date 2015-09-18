package name.abhijitsarkar.scala

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import akka.actor.ActorSystem
import akka.actor.Inbox
import akka.pattern.ask
import scala.concurrent.Promise
import akka.util.Timeout
import scala.concurrent.duration.DurationInt
import scala.concurrent.Await
import scala.concurrent.Future
import name.abhijitsarkar.scala.service.WeatherActor
import akka.actor.Actor

object WeatherApp extends App with ActorPlumbing {
  implicit val timeout = Timeout(1 seconds)
  implicit val ctx = new AnnotationConfigApplicationContext(classOf[AppConfig])

  val system = ctx.getBean(classOf[ActorSystem])

  val beanName = getBeanName(classOf[WeatherActor])

  val prop = SpringExtensionImpl(system).props(beanName)

  // use the Spring Extension to create props for a named actor bean
  val weatherActor = system.actorOf(prop, beanName)
  
  val weather = (weatherActor ? "GetWeather").asInstanceOf[Future[String]]

  println(Await.result(weather, timeout.duration).asInstanceOf[String])

  system.shutdown
  system.awaitTermination

  private def getBeanName(actorClass: Class[_ <: Actor]) = {
    val (firstChar, rest) = actorClass.getSimpleName.splitAt(1)
    
    firstChar.toLowerCase + rest
  }
}


