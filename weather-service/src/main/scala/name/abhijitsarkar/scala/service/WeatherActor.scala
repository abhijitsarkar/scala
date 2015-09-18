package name.abhijitsarkar.scala.service

import org.springframework.beans.factory.annotation.Autowired
import akka.actor.Actor
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class WeatherActor @Autowired() (weatherService: WeatherService) extends Actor {
  def receive = {
    case s: String => sender ! weatherService.currentConditions
  }
}