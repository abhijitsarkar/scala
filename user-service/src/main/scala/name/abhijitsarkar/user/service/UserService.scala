package name.abhijitsarkar.user.service

import name.abhijitsarkar.user.domain.User
import scala.concurrent.Future
import scala.collection._

trait UserService {
  def findByFirstName(firstName: String): Future[immutable.Seq[User]]

  def findByLastName(lastName: String): Future[immutable.Seq[User]]

  def findByFirstAndLastNames(firstName: String, lastName: String): Future[immutable.Seq[User]]

  def createUser(user: User): Future[Option[User]]

  def updateUser(user: User): Future[Option[User]]

  def deleteUser(userId: String): Future[Option[User]]
}
