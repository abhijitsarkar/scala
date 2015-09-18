package name.abhijitsarkar.scala

import akka.actor.ActorSystem
import org.springframework.context.ApplicationContext
import akka.actor.Props
import akka.actor.Extension
import org.springframework.util.Assert._
import akka.actor.Actor

class SpringExtensionImpl extends Extension {
  var applicationContext: ApplicationContext = _

  /**
   * Used to initialize the Spring application context for the extension.
   * @param applicationContext
   */
  def initialize(implicit applicationContext: ApplicationContext) = {
    notNull(applicationContext, "Application context must not be null.")
    
    this.applicationContext = applicationContext

    this
  }

  /**
   * Create a Props for the specified actorBeanName using the
   * ActorProducer class.
   *
   * @param actorBeanName  The name of the actor bean to create Props for
   * @return a Props that will create the named actor bean using Spring
   */
  def props(actorBeanName: String) = {
    notNull(applicationContext, "Application context must not be null.")
    notNull(actorBeanName, "Actor bean name must not be null.")
    
    Props(classOf[BeanNameActorProducer], applicationContext, actorBeanName)
  }  
}

object SpringExtensionImpl {
  def apply(system: ActorSystem)(implicit ctx: ApplicationContext) = SpringExtension(system).initialize(ctx)
}
