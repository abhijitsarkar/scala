package name.abhijitsarkar.user.controller

import scala.collection.immutable.Seq
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.StatusCodes.NotFound
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.TestUtil._
import name.abhijitsarkar.user.controller.UserJsonSupport._
import akka.http.scaladsl.model.ContentTypes.`application/json`

class UserReadResourceSpec extends UserResourceSpec with UserReadResource {
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