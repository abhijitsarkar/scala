package name.abhijitsarkar.user

import scala.collection.immutable.Seq
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import akka.event.NoLogging
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.StatusCodes.Conflict
import akka.http.scaladsl.testkit.ScalatestRouteTest
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.TestUtil._
import name.abhijitsarkar.user.UserJsonSupport._
import name.abhijitsarkar.user.repository.MockUserRepository
import name.abhijitsarkar.user.service.UserService
import akka.http.scaladsl.model.ContentTypes.`application/json`

class UserReadResourceSpec extends FlatSpec with Matchers with ScalatestRouteTest with UserReadResource with ActorPlumbing {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  val userService: UserService = new MockUserRepository

  it should "find a single user with first name John" in {
    Get(s"/user?firstName=John").withHeaders(acceptHeader()) ~> readRoute ~> check {
      status shouldBe OK
      contentType shouldBe (`application/json`)
      val users = responseAs[Seq[User]]

      verifySingleUser(users)
    }
  }

  it should "not find any users with first name Johnny" in {
    Get(s"/user?firstName=Johnny").withHeaders(acceptHeader()) ~> readRoute ~> check {
      status shouldBe NotFound
      contentType shouldBe (`application/json`)
      println(responseAs[String])
    }
  }

  it should "find a single user with last name Doe" in {
    Get(s"/user?lastName=Doe").withHeaders(acceptHeader()) ~> readRoute ~> check {
      status shouldBe OK
      contentType shouldBe (`application/json`)
      val users = responseAs[Seq[User]]

      verifySingleUser(users)
    }
  }

  it should "not find any users with last name Appleseed" in {
    Get(s"/user?lastName=Appleseed").withHeaders(acceptHeader()) ~> readRoute ~> check {
      status shouldBe NotFound
      contentType shouldBe (`application/json`)
      println(responseAs[String])
    }
  }

  it should "find a single user with first name John and last name Doe" in {
    Get(s"/user?firstName=John&lastName=Doe").withHeaders(acceptHeader()) ~> readRoute ~> check {
      status shouldBe OK
      contentType shouldBe (`application/json`)
      val users = responseAs[Seq[User]]

      verifySingleUser(users)
    }
  }

  it should "not find any users with first name Johnny and last name Appleseed" in {
    Get(s"/user?firstName=Johnny&lastName=Appleseed").withHeaders(acceptHeader()) ~> readRoute ~> check {
      status shouldBe NotFound
      contentType shouldBe (`application/json`)
      println(responseAs[String])
    }
  }

  it should "throw exception if neither first nor last name is present" in {
    Get(s"/user").withHeaders(acceptHeader()) ~> readRoute ~> check {
      status shouldBe BadRequest
      contentType shouldBe (`application/json`)
      println(responseAs[String])
    }
  }
}