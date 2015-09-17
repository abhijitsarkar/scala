package name.abhijitsarkar.user.controller

import scala.concurrent.Future
import scala.concurrent.Promise

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.logRequestResult
import akka.http.scaladsl.server.Directives.pathPrefix
import scala.concurrent.Future
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.repository.UserRepository._
import UserJsonSupport._
import akka.actor._
import scala.concurrent.Promise
import akka.pattern.ask
import name.abhijitsarkar.user.ActorPlumbing

trait UserWriteResource extends ActorPlumbing {
  val businessDelegateProps: Props

  // TODO: Content negotiation is not implemented.
  // http://stackoverflow.com/questions/32187858/akka-http-accept-and-content-type-handling
  // http://stackoverflow.com/questions/30859264/test-akka-http-server-using-specs2

  val writeRoute = {
    logRequestResult("user-service") {
      pathPrefix("user") {
        (post & entity(as[User])) { user =>
          path(Segment) { userId =>
            complete {
              val userUpdateRequest = UserUpdateRequest(user)
              val actor = system.actorOf(businessDelegateProps)

              val response = (actor ? userUpdateRequest).asInstanceOf[Promise[Future[UserModificationResponse]]]

              processDeleteOrUpdateResponse(response)
            }
          } ~
            extractUri { requestUri =>
              complete {
                val userCreateRequest = UserCreateRequest(user)
                val actor = system.actorOf(businessDelegateProps)

                val response = (actor ? userCreateRequest).asInstanceOf[Promise[Future[UserModificationResponse]]]

                response.future.flatMap {
                  _.map { response =>
                    val body = response match {
                      case UserModificationResponse(statusCode, Some(uid)) => s"$requestUri/${uid}"
                      case UserModificationResponse(statusCode, _) => s"Failed to create user with id: ${userCreateRequest.user.userId}."
                    }

                    UserModificationResponse(response.statusCode, Some(body))
                  }
                }
              }
            }
        } ~
          (delete & path(Segment)) { userId =>
            complete {
              val userDeleteRequest = UserDeleteRequest(userId)
              val actor = system.actorOf(businessDelegateProps)

              val response = (actor ? userDeleteRequest).asInstanceOf[Promise[Future[UserModificationResponse]]]

              processDeleteOrUpdateResponse(response)
            }
          }
      }
    }
  }

  def processDeleteOrUpdateResponse(response: Promise[Future[UserModificationResponse]]) = {
    response.future.flatMap { _.map { identity } }
  }
}
