package name.abhijitsarkar.user.service

import scala.collection.immutable.Seq
import scala.concurrent._
import akka.actor.Actor
import akka.actor.ActorLogging
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.service.UserService._
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.actor.Props
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import akka.stream.actor.ActorPublisher
import scala.annotation.tailrec
import akka.stream.actor.ActorPublisherMessage.{ Cancel, Request }

class UserBusinessDelegate(private val userService: UserService)(private implicit val executor: ExecutionContextExecutor)
    extends ActorPublisher[Future[UserServiceResponse]] with ActorLogging {
  val maxBufferSize = 100
  var buff = Vector.empty[Future[UserServiceResponse]]

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

  def isBufferFull = (buff.size == maxBufferSize)

  def isBufferEmpty = (buff.size == 0)

  def process(response: Future[UserServiceResponse]) = {
    sender ! Future.successful(Accepted)

    buff :+= response

    deliver
  }

  @tailrec final def deliver: Unit = {
     println(s"totalDemand: $totalDemand, isActive: $isActive, buffer empty: ${isBufferEmpty}, buff: $buff")
    
    if (totalDemand > 0 && isActive && !isBufferEmpty) {
      val (use, keep) = buff.splitAt(totalDemand.toInt)
      buff = keep
      
      use foreach { item => println(item); onNext(item) }
      deliver
    }
  }

  def receive = {
    case _ if (isBufferFull) => sender ! Future.failed(new StackOverflowError)

    case FindByNameRequest(Some(firstName), None) => {
      val cleansedFirstName = cleanse(firstName)

      val future = userService.findByFirstName(cleansedFirstName).map { prettifyUsers }

      process(future.map { processFindByNameResults(_) })
    }

    case FindByNameRequest(None, Some(lastName)) => {
      val cleansedLastName = cleanse(lastName)

      val future = userService.findByLastName(cleanse(cleansedLastName)).map { prettifyUsers }

      process(future.map { processFindByNameResults(_) })
    }

    case FindByNameRequest(Some(firstName), Some(lastName)) => {
      val cleansedFirstName = cleanse(firstName)
      val cleansedLastName = cleanse(lastName)

      val future = userService.findByFirstAndLastNames(cleansedFirstName, cleansedLastName).map { prettifyUsers }

      process(future.map { processFindByNameResults(_) })
    }

    case FindByNameRequest(_, _) => sender ! process(Future.successful(FindByNameResponse(StatusCodes.BadRequest, Seq.empty[User])))

    case UserUpdateRequest(user) => process(userService.updateUser(cleanseUser(user)).map {
      userModificationResponseWithStatusCode(_, NotFound)
    })

    case UserCreateRequest(user) => process(userService.createUser(cleanseUser(user)).map {
      userModificationResponseWithStatusCode(_, Conflict, Created)
    })

    case UserDeleteRequest(userId) => process(userService.deleteUser(cleanse(userId)).map {
      userModificationResponseWithStatusCode(_, NotFound)
    })

    case Request(_) => deliver
    case Cancel => context.stop(self)
  }

  private[user] def userModificationResponseWithStatusCode(user: Option[User], failureStatus: StatusCode,
    successStatus: StatusCode = OK) = {
    UserModificationResponse(
      statusCode = user match {
        case Some(user) => successStatus
        case _ => failureStatus
      },
      body = user.map { prettifyUser })
  }

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

object UserBusinessDelegate {
  def props(userService: UserService, executor: ExecutionContextExecutor) = Props(new UserBusinessDelegate(userService)(executor))
}