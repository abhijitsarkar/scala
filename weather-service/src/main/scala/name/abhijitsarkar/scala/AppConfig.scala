package name.abhijitsarkar.scala

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import akka.actor.ActorSystem
import akka.actor.Props
import name.abhijitsarkar.scala.service.WeatherService

@Configuration
@ComponentScan
class AppConfig {
  @Autowired
  implicit var ctx: ApplicationContext = _

  @Bean
  def actorSystem() = {
    val system = ActorSystem("AkkaScalaSpring")
    SpringExtension(system)

    system
  }
}