package name.abhijitsarkar.user.repository

import scala.collection.immutable.Seq
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import name.abhijitsarkar.user.ActorPlumbing
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.service.UserService
import akka.stream.Materializer

class MongoDBUserRepositoryAdapter(private val materializer: Materializer, private val userRepository: MongoDBUserRepository) extends UserService {
  override def findByFirstName(firstName: String) = {
    val users = userRepository.findByFirstName(firstName)

    transform(users)
  }

  override def findByLastName(lastName: String) = {
    val users = userRepository.findByLastName(lastName)

    transform(users)
  }

  override def findByFirstAndLastNames(firstName: String, lastName: String) = {
    val users = userRepository.findByFirstAndLastNames(firstName, lastName)

    transform(users)
  }

  override def createUser(user: User) = {
    val usr = userRepository.createUser(user)

    transform(usr)
  }

  override def updateUser(user: User) = {
    val usr = userRepository.updateUser(user)

    transform(usr)
  }

  override def deleteUser(userId: String) = {
    val user = userRepository.deleteUser(userId)

    transform(user)
  }

  private def transform(user: Option[User]) = {
    val src = Source.single(user)
    val sink = Sink.head[Option[User]]

    src.runWith(sink)(materializer)
  }

  private def transform(users: Seq[User]) = {
    val src = Source(users)
    val sink = Sink.fold[Seq[User], User](Seq.empty)(_ :+ _)

    src.runWith(sink)(materializer)
  }
}