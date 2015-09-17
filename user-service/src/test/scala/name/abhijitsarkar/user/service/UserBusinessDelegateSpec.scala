package name.abhijitsarkar.user.service

import scala.collection.immutable.Seq
import scala.concurrent.duration.DurationInt
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers
import akka.util.Timeout
import name.abhijitsarkar.user.repository.MockUserRepository
import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import akka.actor.Props
import name.abhijitsarkar.user.repository.UserRepository._
import scala.concurrent.ExecutionContextExecutor
import name.abhijitsarkar.user.repository.MockUserRepository
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.StatusCodes._
import scala.concurrent.Promise
import scala.concurrent.Await
import name.abhijitsarkar.user.domain.User
import akka.http.scaladsl.model.StatusCode

class UserBusinessDelegateSpec extends TestKit(ActorSystem("user-service")) with ImplicitSender with FlatSpecLike with Matchers with BeforeAndAfterAll {
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  val duration = 1 seconds

  val userRepository = new MockUserRepository
  //  val mockUsers = Seq(userRepository.mockUser)
  val mockUser = User(Some("1"), "John", "Doe", "555-555-5555", Some("johndoe@gmail.com"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val businessDelegate = system.actorOf(UserBusinessDelegate.props(userRepository, executor))

  def pf[A]: PartialFunction[Any, A] = {
    case p: Promise[A] => {
      Await.result[A](p.future, duration)
    }
  }

  def findByNameAndThenVerify(request: FindByNameRequest) = {
    businessDelegate ! request

    val actual = expectMsgPF[FindByNameResponse](duration)(pf)

    actual.statusCode shouldBe (OK)
    actual.body should contain(mockUser)
  }

  it should "trim leading and trailing spaces from first name search results" in {
    findByNameAndThenVerify(FindByNameRequest(Some(" john "), None))
  }

  it should "trim leading and trailing spaces from last name search results" in {
    findByNameAndThenVerify(FindByNameRequest(None, Some(" Doe ")))
  }

  it should "trim leading and trailing spaces from first and last names search results" in {
    findByNameAndThenVerify(FindByNameRequest(Some(" john "), Some(" doe ")))
  }

  it should "camel case first name search results" in {
    findByNameAndThenVerify(FindByNameRequest(Some("john"), None))
  }

  it should "camel case last name search results" in {
    findByNameAndThenVerify(FindByNameRequest(None, Some("doe")))
  }

  it should "camel case first and last names search results" in {
    findByNameAndThenVerify(FindByNameRequest(Some("john"), Some("doe")))
  }

  it should "reject a name search with neither a first name nor a last" in {
    businessDelegate ! FindByNameRequest(None, None)

    val actual = expectMsgPF[FindByNameResponse](duration)(pf)

    actual.statusCode shouldBe (BadRequest)
    actual.body shouldBe empty
  }

  it should "process user update request" in {
    businessDelegate ! UserUpdateRequest(mockUser)

    val actual = expectMsgPF[UserModificationResponse](duration)(pf)

    actual.statusCode shouldBe (OK)
    actual.body shouldBe defined
  }

  it should "reject phone number if not 10 digits long during user update" in {
    businessDelegate ! UserUpdateRequest(mockUser.copy(phoneNum = "555"))

    val actual = expectMsgPF[StatusCode](duration)(pf)

    actual shouldBe BadRequest
  }

  it should "process user create request" in {
    businessDelegate ! UserCreateRequest(mockUser)

    val actual = expectMsgPF[UserModificationResponse](duration)(pf)

    actual.statusCode shouldBe (Created)
    actual.body shouldBe defined
  }

  it should "process user delete request" in {
    businessDelegate ! UserDeleteRequest(mockUser.userId.get)

    val actual = expectMsgPF[UserModificationResponse](duration)(pf)

    actual.statusCode shouldBe (OK)
    actual.body shouldBe defined
  }

  // TODO: More tests  
}