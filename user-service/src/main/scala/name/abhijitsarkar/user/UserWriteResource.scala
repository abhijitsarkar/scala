package name.abhijitsarkar.user

import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.StatusCodes.Conflict
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.logRequestResult
import akka.http.scaladsl.server.Directives.pathPrefix
import scala.concurrent.Future

import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.service.UserService
import name.abhijitsarkar.user.service.UserService._
import name.abhijitsarkar.user.UserJsonSupport._
import akka.actor._
import akka.stream.scaladsl.Source
import akka.stream._

trait UserWriteResource extends ActorPlumbing {
  val userService: UserService

  // TODO: Content negotiation is not implemented.
  // http://stackoverflow.com/questions/32187858/akka-http-accept-and-content-type-handling
  // http://stackoverflow.com/questions/30859264/test-akka-http-server-using-specs2

  val writeRoute = {
    logRequestResult("user-service") {
      pathPrefix("user") {
        (put & entity(as[User])) { user =>
          extractUri { requestUri =>
            val source = Source.actorRef[UserModificationResponse](10, OverflowStrategy.fail)
              .mapMaterializedValue(ref ⇒ ref ! UserCreateRequest(user))

            val emptyResponse = (0, "")

            val result = source.runFold(emptyResponse) { (_, response) =>
              response match {
                case UserModificationResponse(statusCode, Some(user)) => (statusCode.intValue, s"$requestUri/${user.userId.get}")
                case UserModificationResponse(statusCode, _) => (statusCode.intValue, s"Failed to create user with id: ${user.userId}.")
              }
            }

            complete(result.map(identity))
          }
        } ~
          (post & entity(as[User])) { user =>
            val source = Source.actorRef[UserModificationResponse](10, OverflowStrategy.fail)
              .mapMaterializedValue(ref ⇒ ref ! UserUpdateRequest(user))

            val result = processDeleteOrUpdateResponse(source)
            complete(result.map(identity))
          } ~
          (delete & path(Segment)) { userId =>
            val source = Source.actorRef[UserModificationResponse](10, OverflowStrategy.fail)
              .mapMaterializedValue(ref ⇒ ref ! UserDeleteRequest(userId))

            val result = processDeleteOrUpdateResponse(source)
            complete(result.map(identity))
          }
      }
    }
  }

  def processDeleteOrUpdateResponse(source: Source[UserModificationResponse, Unit]) = {
    val emptyResponse = (0, Option.empty[User])

    source.runFold(emptyResponse) { (_, response) => (response.statusCode.intValue, response.body) }
  }
}
