package name.abhijitsarkar.scala

import akka.actor.IndirectActorProducer
import akka.actor.Actor
import org.springframework.context.ApplicationContext
import org.springframework.util.Assert._

class BeanNameActorProducer(ctx: ApplicationContext, actorBeanName: String) extends IndirectActorProducer {

  override def produce: Actor = {
    ctx.getBean(actorBeanName, classOf[Actor])
  }

  override def actorClass: Class[_ <: Actor] = {
    notNull(ctx, "Application context must not be null.")
    notNull(actorBeanName, "Actor bean name must not be null.")

    ctx.getType(actorBeanName).asInstanceOf[Class[Actor]]
  }
}