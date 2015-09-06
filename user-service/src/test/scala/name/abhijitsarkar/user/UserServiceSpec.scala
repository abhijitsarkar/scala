package name.abhijitsarkar.user

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.sprayJsonMarshaller
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.ScalatestRouteTest
import name.abhijitsarkar.user.domain.User

class UserServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  "User service" should "extract first name from query param" in {
    Get(s"/user?firstName=John") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Hello John"
    }
  }

  "User service" should "extract last name from query param" in {
    Get(s"/user?lastName=Doe") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Hello Doe"
    }
  }

  "User service" should "extract both first and last names from query param" in {
    Get(s"/user?firstName=John&lastName=Doe") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Hello John Doe"
    }
  }

  "User service" should "throw exception if neither first nor last name is present" in {
    Get(s"/user") ~> route ~> check {
      status shouldBe BadRequest
    }
  }
  
  "User service" should "extract both first and last names from create request body" in {
    val user = User("1", "John", "Doe", "555-555-5555", None)
    
    Put(s"/user", user) ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Hello John Doe"
    }
  }
  
  "User service" should "extract both first and last names from update request body" in {
    val user = User("1", "John", "Doe", "555-555-5555", None)
    
    Post(s"/user", user) ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Hello John Doe"
    }
  }
  
  "User service" should "extract user id from delete path segment" in {
    Delete(s"/user/1") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Hello 1"
    }
  }
}