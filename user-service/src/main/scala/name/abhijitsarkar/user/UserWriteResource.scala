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
import name.abhijitsarkar.user.UserJsonSupport._

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
            val future = userService.createUser(user)

            val result = future.map {
              case Some(user) => Response(Created, s"$requestUri/${user.userId.get}")
              case _ => Response(Conflict, s"Failed to create user with id: ${user.userId}.")
            }

            complete(result)
          }
        } ~
          (post & entity(as[User])) { user =>
            val future = userService.updateUser(user)

            val result = future.map { processDeleteOrUpdateResponse }
            complete(result)
          } ~
          (delete & path(Segment)) { userId =>
            val future = userService.deleteUser(userId)

            val result = future.map { processDeleteOrUpdateResponse }
            complete(result)
          }
      }
    }
  }

  def processDeleteOrUpdateResponse(user: Option[User]) = {
    user match {
      case Some(user) => Response(OK, user.userId.get)
      case _ => Response(NotFound, "No user found.")
    }
  }
}
