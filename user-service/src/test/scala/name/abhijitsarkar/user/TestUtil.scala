package name.abhijitsarkar.user

import scala.collection.immutable.Seq
import org.scalatest.Matchers
import akka.http.scaladsl.model.headers.Accept
import name.abhijitsarkar.user.domain.User
import scala.util.Random

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

  private val randomGenerator = new Random
  private def random(maxExclusive: Int = 10) = randomGenerator.nextInt(maxExclusive)

  // Generates a random number between 97 (inclusive) and 123 (exclusive) and converts it to ASCII. Does this 5 times,
  // concats the result and calls it a user id.
  def randomUserId = s"${(1 to 5).map { _ => random(26) + 97 }.map {_.toChar}.mkString}"
  
  def randomEmail = s"${randomUserId}@test.com"
  def randomPhoneNum = random().toString * 10
}