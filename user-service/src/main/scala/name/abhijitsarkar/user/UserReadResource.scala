package name.abhijitsarkar.user

import scala.collection.immutable.Seq
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.logRequestResult
import akka.http.scaladsl.server.Directives.parameters
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.model.StatusCodes.ClientError
import scala.concurrent.Future
import akka.http.scaladsl.model.StatusCode

import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.service.UserService
import name.abhijitsarkar.user.UserJsonSupport._

trait UserReadResource extends ActorPlumbing {
  val userService: UserService

  case class Name(firstName: Option[String], lastName: Option[String])
  
  implicit val nameJsonFormat = jsonFormat2(Name)

  def findUsersByName(name: Name): Future[Either[Seq[User], Response]] = {
    def isFound(users: Seq[User]) = {
      !users.isEmpty
    }

    name match {
      case Name(Some(firstName), None) => {
        val future = userService.findByFirstName(firstName)
        future.map { users => if (isFound(users)) Left(users) else Right(Response(NotFound, s"No users found with first name: $firstName.")) }
      }
      case Name(None, Some(lastName)) => {
        val future = userService.findByLastName(lastName)
        future.map { users => if (isFound(users)) Left(users) else Right(Response(NotFound, s"No users found with last name: $lastName.")) }
      }
      case Name(Some(firstName), Some(lastName)) => {
        val future = userService.findByFirstAndLastNames(firstName, lastName)
        future.map { users => if (isFound(users)) Left(users) else Right(Response(NotFound, s"No users found with first name: $firstName and last name: $lastName.")) }
      }
      case _ => Future.successful(Right(Response(BadRequest, "One of first and last names is required.")))
    }
  }

  // TODO: Content negotiation is not implemented.
  // http://stackoverflow.com/questions/32187858/akka-http-accept-and-content-type-handling
  // http://stackoverflow.com/questions/30859264/test-akka-http-server-using-specs2
  val readRoute = {
    logRequestResult("user-service") {
      pathPrefix("user") {
        get {
          parameters("firstName".?, "lastName".?).as(Name) { name =>
            complete(findUsersByName(name).map(identity))
          }
        }
      }
    }
  }
}
