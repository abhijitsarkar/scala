package name.abhijitsarkar.user.service

import scala.collection.immutable.Seq
import scala.concurrent.Future

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import name.abhijitsarkar.user.domain.User

trait UserService {
  def findByFirstName(firstName: String): Future[Seq[User]]

  def findByLastName(lastName: String): Future[Seq[User]]

  def findByFirstAndLastNames(firstName: String, lastName: String): Future[Seq[User]]

  def createUser(user: User): Future[Option[User]]

  def updateUser(user: User): Future[Option[User]]

  def deleteUser(userId: String): Future[Option[User]]
}

object UserService {
  sealed trait UserServiceRequest
  sealed trait UserServiceResponse
  
  case class FindByNameRequest(firstName: Option[String], lastName: Option[String]) extends UserServiceRequest
  case class FindByNameResponse(statusCode: StatusCode, body: Seq[User]) extends UserServiceResponse

  case class UserUpdateRequest(user: User) extends UserServiceRequest
  case class UserCreateRequest(user: User) extends UserServiceRequest
  case class UserDeleteRequest(userId: String) extends UserServiceRequest
  case class UserModificationResponse(statusCode: StatusCode = OK, body: Option[User]) extends UserServiceResponse
}

