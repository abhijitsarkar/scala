package name.abhijitsarkar.user.controller

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.StatusCodes.Conflict
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.TestUtil._
import name.abhijitsarkar.user.controller.UserJsonSupport._
import akka.http.scaladsl.model.ContentTypes.`application/json`

class UserWriteResourceSpec extends UserResourceSpec with UserWriteResource {
  it should "create new user and return the resource URI" in {
    val user = User(Some("1"), "John", "Doe", "555-555-5555", None)

    Post(s"/user", user).withHeaders(acceptHeader()) ~> writeRoute ~> check {
      status shouldBe Created
      contentType shouldBe (`application/json`)
      responseAs[String] should endWith("/user/1\"")
      println(responseAs[String])
    }
  }

  it should "not create new user" in {
    val user = User(Some("junk"), "John", "Doe", "555-555-5555", None)

    Post(s"/user", user).withHeaders(acceptHeader()) ~> writeRoute ~> check {
      status shouldBe Conflict
      contentType shouldBe (`application/json`)
      println(responseAs[String])
    }
  }

  it should "update existing user and return the user id" in {
    val user = User(Some("1"), "John", "Doe", "555-555-5555", None)

    Post(s"/user/1", user).withHeaders(acceptHeader()) ~> writeRoute ~> check {
      status shouldBe OK
      contentType shouldBe (`application/json`)
      responseAs[String] shouldBe "\"1\""
      println(responseAs[String])
    }
  }

  it should "not update non existing user" in {
    val user = User(Some("junk"), "John", "Doe", "555-555-5555", None)

    Post(s"/user/junk", user).withHeaders(acceptHeader()) ~> writeRoute ~> check {
      status shouldBe NotFound
      contentType shouldBe (`application/json`)
      println(responseAs[String])
    }
  }

  it should "delete existing user and return the user id" in {
    Delete(s"/user/1").withHeaders(acceptHeader()) ~> writeRoute ~> check {
      status shouldBe OK
      contentType shouldBe (`application/json`)
      responseAs[String] shouldBe "\"1\""
      println(responseAs[String])
    }
  }

  it should "not delete non existing user" in {
    Delete(s"/user/junk").withHeaders(acceptHeader()) ~> writeRoute ~> check {
      status shouldBe NotFound
      contentType shouldBe (`application/json`)
      println(responseAs[String])
    }
  }
}