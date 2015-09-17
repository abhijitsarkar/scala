package name.abhijitsarkar.user.repository

import scala.collection.immutable.Seq
import scala.concurrent.Future
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import name.abhijitsarkar.user.domain.User
import scala.util.Try

trait UserRepository {
  def findByFirstName(firstName: String): Future[Seq[User]]

  def findByLastName(lastName: String): Future[Seq[User]]

  def findByFirstAndLastNames(firstName: String, lastName: String): Future[Seq[User]]

  def findById(userId: String): Future[Option[User]]

  def createUser(user: User): Future[Option[String]]

  def updateUser(user: User): Future[Option[String]]

  def deleteUser(userId: String): Future[Option[String]]
}

object UserRepository {
  sealed trait FindUserRequest
  sealed trait FindUserResponse

  case class FindByNameRequest(firstName: Option[String], lastName: Option[String]) extends FindUserRequest
  case class FindByNameResponse(statusCode: StatusCode, body: Seq[User]) extends FindUserResponse
  case class FindByIdRequest(userId: String) extends FindUserRequest
  case class FindByIdResponse(statusCode: StatusCode, body: Option[User]) extends FindUserResponse

  sealed trait UserModificationRequest

  case class UserUpdateRequest(user: User) extends UserModificationRequest
  case class UserCreateRequest(user: User) extends UserModificationRequest
  case class UserDeleteRequest(userId: String) extends UserModificationRequest
  case class UserModificationResponse(statusCode: StatusCode = OK, body: Option[String])
}

