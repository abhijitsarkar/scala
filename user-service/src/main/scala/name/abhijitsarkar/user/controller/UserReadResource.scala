package name.abhijitsarkar.user.controller

import scala.concurrent.Future
import scala.concurrent.Promise

import UserJsonSupport.findByNameResponseMarshaller
import akka.actor.Props
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.server.Directive.addByNameNullaryApply
import akka.http.scaladsl.server.Directive.addDirectiveApply
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Directives.get
import akka.http.scaladsl.server.Directives.logRequestResult
import akka.http.scaladsl.server.Directives.parameters
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Directives.segmentStringToPathMatcher
import akka.http.scaladsl.server.Directives.string2NR
import akka.http.scaladsl.server.directives.LoggingMagnet.forRequestResponseFromMarker
import akka.http.scaladsl.server.util.ConstructFromTuple.instance2
import akka.pattern.ask
import name.abhijitsarkar.user.ActorPlumbing
import name.abhijitsarkar.user.repository.UserRepository.FindByNameRequest
import name.abhijitsarkar.user.repository.UserRepository.FindByNameResponse

trait UserReadResource extends ActorPlumbing {
  val businessDelegateProps: Props
  // TODO: Content negotiation is not implemented.
  // http://stackoverflow.com/questions/32187858/akka-http-accept-and-content-type-handling
  // http://stackoverflow.com/questions/30859264/test-akka-http-server-using-specs2
  val readRoute = {
    logRequestResult("user-service") {
      pathPrefix("user") {
        get {
          parameters("firstName".?, "lastName".?).as(FindByNameRequest) { name =>
            val actor = system.actorOf(businessDelegateProps)

            val response = (actor ? name).asInstanceOf[Promise[Future[FindByNameResponse]]]

            complete(response.future.flatMap { identity })
          }
        }
      }
    }
  }
}
