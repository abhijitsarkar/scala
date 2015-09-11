package name.abhijitsarkar.user

import scala.collection.immutable.Seq

import org.scalatest.Matchers

import akka.http.scaladsl.model.headers.Accept
import name.abhijitsarkar.user.domain.User

object TestUtil extends Matchers {
  def verifySingleUser(users: Seq[User], expectedFirstName: String = "John", expectedLastName: String = "Doe") {
    users should have size (1)

    val user = users.head

    user.firstName shouldBe (expectedFirstName)
    user.lastName shouldBe (expectedLastName)
  }

  def acceptHeader() = {
    Accept.parseFromValueString("application/json").right.get
  }
}