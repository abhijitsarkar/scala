package name.abhijitsarkar.scala

import akka.actor.AbstractExtensionId
import akka.actor.ExtendedActorSystem
import akka.actor.ExtensionIdProvider

object SpringExtension extends AbstractExtensionId[SpringExtensionImpl] with ExtensionIdProvider {
  /**
   * Is used by Akka to instantiate the Extension identified by this
   * ExtensionId, internal use only.
   */
  override def createExtension(system: ExtendedActorSystem) = new SpringExtensionImpl

  /**
   * Retrieve the SpringExtension extension for the given system.
   */
  override def lookup() = SpringExtension
}