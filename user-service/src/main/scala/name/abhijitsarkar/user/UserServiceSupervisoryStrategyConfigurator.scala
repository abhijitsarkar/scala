package name.abhijitsarkar.user

import akka.actor.SupervisorStrategyConfigurator
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy

class UserServiceSupervisoryStrategyConfigurator extends SupervisorStrategyConfigurator {
  override def create = {
    OneForOneStrategy() {
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }
  }
}