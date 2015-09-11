package name.abhijitsarkar.user.repository

import scala.concurrent.ExecutionContext.Implicits.global
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.service.UserService
import scala.concurrent.Future
import name.abhijitsarkar.user.ActorPlumbing
import scala.collection.immutable.Seq

class MockUserRepository extends UserService {
  private val mockUser = User(Some("1"), "John", "Doe", "555-555-5555", Some("johndoe@gmail.com"))

  private def toFuture[A](anything: A) = {
    Future(anything)
  }

  override def findByFirstName(firstName: String) = {
    val result = if (firstName.equalsIgnoreCase(mockUser.firstName)) Seq[User](mockUser) else Seq.empty[User]
    toFuture[Seq[User]](result)
  }

  override def findByLastName(lastName: String) = {
    val result = if (lastName.equalsIgnoreCase(mockUser.lastName)) Seq[User](mockUser) else Seq.empty[User]
    toFuture[Seq[User]](result)
  }

  override def findByFirstAndLastNames(firstName: String, lastName: String) = {
    val result = if (firstName.equalsIgnoreCase(mockUser.firstName) && lastName.equalsIgnoreCase(mockUser.lastName))
      Seq[User](mockUser)
    else Seq.empty[User]

    toFuture[Seq[User]](result)
  }

  override def updateUser(user: User) = {
    val result = if (user.userId == mockUser.userId) Some(mockUser) else None

    toFuture[Option[User]](result)
  }

  override def createUser(user: User) = {
    val result = if (user.userId == mockUser.userId) Some(mockUser) else None

    toFuture[Option[User]](result)
  }

  override def deleteUser(userId: String) = {
    val result = if (userId == mockUser.userId.get) Some(mockUser) else None

    toFuture[Option[User]](result)
  }
}
