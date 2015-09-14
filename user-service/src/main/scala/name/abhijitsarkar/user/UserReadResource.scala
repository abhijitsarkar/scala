package name.abhijitsarkar.user

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.logRequestResult
import akka.http.scaladsl.server.Directives.parameters
import akka.http.scaladsl.server.Directives.pathPrefix
import scala.concurrent.Future
import akka.http.scaladsl.model.StatusCodes._
import scala.collection.immutable.Seq
import name.abhijitsarkar.user.service.UserService._
import name.abhijitsarkar.user.UserJsonSupport._
import akka.actor.Props
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink

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
            type FindResponse = Future[FindByNameResponse]
              
            val src: Source[FindResponse, Unit] = Source.actorPublisher[FindResponse](businessDelegateProps).mapMaterializedValue {
              _ ! name
            }
            val emptyResponse = Future.apply(FindByNameResponse(OK, Seq.empty))

//            val sink: Sink[FindResponse, FindByNameResponse] = Sink.fold(emptyResponse)((_, response: FindResponse) => response.map { x => x })
//            val sink = Sink.head[FindResponse].mapMaterializedValue { x => x.flatMap { y => y } }
            
            val response = src.runFold(emptyResponse)((_, response: FindResponse) => response).flatMap { identity }

            complete(response)
          }
        }
      }
    }
  }
}
