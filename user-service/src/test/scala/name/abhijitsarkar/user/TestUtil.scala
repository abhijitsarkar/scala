package name.abhijitsarkar.user

import name.abhijitsarkar.user.domain.User
import org.scalatest.Matchers

object TestUtil extends Matchers {
  def verifySingleUser(users: Seq[User], expectedFirstName: String = "John", expectedLastName: String = "Doe") {
    users should have size (1)

    val user = users.head

    user.firstName shouldBe (expectedFirstName)
    user.lastName shouldBe (expectedLastName)
  }
}