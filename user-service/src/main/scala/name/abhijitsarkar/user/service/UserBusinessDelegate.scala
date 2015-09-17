package name.abhijitsarkar.user.service

import scala.collection.immutable.Seq
import scala.concurrent._
import akka.actor.Actor
import akka.actor.ActorLogging
import name.abhijitsarkar.user.domain.User
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.actor.Props
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.collection.mutable.ArrayBuffer
import akka.stream.actor.ActorPublisher
import scala.annotation.tailrec
import name.abhijitsarkar.user.repository.UserRepository._
import name.abhijitsarkar.user.repository.UserRepository

class UserBusinessDelegate(private val userRepository: UserRepository)(private implicit val executor: ExecutionContextExecutor)
    extends Actor with ActorLogging {
  import UserBusinessDelegate._

  override def postStop() {
    log.info(s"Stopping ${getClass.getName}")

    // Supervisory strategy defined by UserServiceSupervisoryStrategyConfigurator and configured in application.conf
    sender ! Future.successful(BadRequest)
  }

  def processFindByNameResults(results: Seq[User]) = {
    results match {
      case Nil => FindByNameResponse(NotFound, Seq.empty[User])
      case _ => FindByNameResponse(OK, results)
    }
  }

  def receive = {
    case FindByNameRequest(Some(firstName), None) => {
      val cleansedFirstName = cleanse(firstName)

      val future = userRepository.findByFirstName(cleansedFirstName).map { prettifyUsers }

      sender ! future.map { processFindByNameResults(_) }
    }

    case FindByNameRequest(None, Some(lastName)) => {
      val cleansedLastName = cleanse(lastName)

      val future = userRepository.findByLastName(cleanse(cleansedLastName)).map { prettifyUsers }

      sender ! future.map { processFindByNameResults(_) }
    }

    case FindByNameRequest(Some(firstName), Some(lastName)) => {
      val cleansedFirstName = cleanse(firstName)
      val cleansedLastName = cleanse(lastName)

      val future = userRepository.findByFirstAndLastNames(cleansedFirstName, cleansedLastName).map { prettifyUsers }

      sender ! future.map { processFindByNameResults(_) }
    }

    case FindByNameRequest(_, _) => sender ! Future.successful(FindByNameResponse(StatusCodes.BadRequest, Seq.empty[User]))

    case FindByIdRequest(userId) => userRepository.findById(cleanse(userId)).map {
      _ match {
        case Some(user) => FindByIdResponse(OK, Some(user))
        case _ => FindByIdResponse(NotFound, None)
      }
    }

    case UserUpdateRequest(user) => sender ! userRepository.updateUser(cleanseUser(user)).map {
      userModificationResponseWithStatusCode(_, NotFound)
    }

    case UserCreateRequest(user) => sender ! userRepository.createUser(cleanseUser(user)).map {
      userModificationResponseWithStatusCode(_, Conflict, Created)
    }

    case UserDeleteRequest(userId) => sender ! userRepository.deleteUser(cleanse(userId)).map {
      userModificationResponseWithStatusCode(_, NotFound)
    }

    case _ => sender ! Future.successful(BadRequest)
  }

  private[user] def userModificationResponseWithStatusCode(userId: Option[String], failureStatus: StatusCode,
    successStatus: StatusCode = OK) = {
    UserModificationResponse(
      statusCode = userId match {
        case Some(uid) => successStatus
        case _ => failureStatus
      },
      body = userId.map { identity })
  }
}

object UserBusinessDelegate {
  def props(userRepository: UserRepository, executor: ExecutionContextExecutor) = Props(new UserBusinessDelegate(userRepository)(executor))

  private[user] def isNotNullOrEmpty(input: String) = {
    input != null && !input.trim.isEmpty
  }

  private[user] def prettifyUsers(users: Seq[User]) = {
    users.map { prettifyUser }
  }

  private[user] def prettifyUser(user: User) = {
    val firstName = user.firstName.capitalize
    val lastName = user.lastName.capitalize
    val phoneNum = prettifyPhoneNum(user.phoneNum)

    user.copy(firstName = firstName, lastName = lastName, phoneNum = phoneNum)
  }

  private[user] def prettifyPhoneNum(phoneNum: String) = {
    val (areaCodeAndPrefix, lineNum) = phoneNum.splitAt(6)

    (areaCodeAndPrefix.splitAt(3).productIterator).mkString("-") + "-" + lineNum
  }

  private[user] def cleanseUser(user: User) = {
    val userId = user.userId.map { cleanse }

    val firstName = cleanse(user.firstName)
    val lastName = cleanse(user.lastName)
    val phoneNum = cleansePhoneNum(user.phoneNum)

    val email = user.email.map { cleanse }

    user.copy(userId = userId, firstName = firstName, lastName = lastName, phoneNum = phoneNum,
      email = email)
  }

  private[user] def cleanse(data: String) = {
    require(isNotNullOrEmpty(data), "Null or empty data.")

    data.trim.toLowerCase
  }

  private[user] def cleansePhoneNum(phoneNum: String) = {
    val phone = cleanse(phoneNum).filterNot { c => (c == '-') || (c == '.') }

    require(phone.size == 10, "Phone number must be 10 digits.")

    phone
  }
}