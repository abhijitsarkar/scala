package name.abhijitsarkar.user

import scala.concurrent.ExecutionContextExecutor
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.StatusCodes.Conflict
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.logRequestResult
import akka.http.scaladsl.server.Directives.parameters
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.stream.Materializer
import name.abhijitsarkar.user.domain.User
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.mongodb.casbah.MongoClient
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.MediaTypes.`application/json`

object UserJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val createAndUpdateRequestFormat = jsonFormat5(User)
}

trait UserService {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  val userRepository: UserRepository

  def config: Config
  val logger: LoggingAdapter

  case class Name(firstName: Option[String], lastName: Option[String])

  import UserJsonSupport._

  def findUserByName(name: Name) = {
    def isFound(users: Seq[User]) = {
      !users.isEmpty
    }

    name match {
      case Name(Some(firstName), None) => {
        val users = userRepository.findByFirstName(firstName)
        if (isFound(users)) Left(users) else Right(NotFound -> s"No users found with first name: $firstName.")
      }
      case Name(None, Some(lastName)) => {
        val users = userRepository.findByLastName(lastName)
        if (isFound(users)) Left(users) else Right(NotFound -> s"No users found with last name: $lastName.")
      }
      case Name(Some(firstName), Some(lastName)) => {
        val users = userRepository.findByFirstAndLastNames(firstName, lastName)
        if (isFound(users)) Left(users) else Right(NotFound -> s"No users found with first name: $firstName and last name: $lastName.")
      }
      case _ => Right(BadRequest -> "One of first and last names is required.")
    }
  }

  def processDeleteOrUpdateResponse(user: Option[User]) = {
    user match {
      case Some(user) => complete(OK, HttpEntity(`application/json`, user.userId.get))
      case None => complete(NotFound, HttpEntity(`application/json`, "No user found."))
    }
  }

  // TODO: Content negotiation is not implemented.
  // http://stackoverflow.com/questions/32187858/akka-http-accept-and-content-type-handling
  val route = {
    logRequestResult("user-service") {
      pathPrefix("user") {
        get {
          parameters("firstName".?, "lastName".?).as(Name) { name =>
            findUserByName(name) match {
              case Left(users) => complete(users)
              case Right(error) => complete(error._1, HttpEntity(`application/json`, error._2))
            }
          }
        } ~
          (put & entity(as[User])) { user =>
            val newUser = userRepository.createUser(user)

            newUser match {
              case Some(user) => {
                extractUri { requestUri =>
                  complete(Created, HttpEntity(`application/json`, s"$requestUri/${user.userId.get}"))
                }
              }
              case None => complete(Conflict, HttpEntity(`application/json`, s"Failed to create user with id: ${user.userId}."))
            }
          } ~
          (post & entity(as[User])) { user =>
            val updatedUser = userRepository.updateUser(user)

            processDeleteOrUpdateResponse(updatedUser)
          } ~
          (delete & path(Segment)) { userId =>
            val deletedUser = userRepository.deleteUser(userId)

            processDeleteOrUpdateResponse(deletedUser)
          }
      }
    }
  }
}
