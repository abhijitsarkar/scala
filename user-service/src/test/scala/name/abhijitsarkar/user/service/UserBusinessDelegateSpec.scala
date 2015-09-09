package name.abhijitsarkar.user.service

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import name.abhijitsarkar.user.TestUtil._
import name.abhijitsarkar.user.repository.MockUserRepository

class UserBusinessDelegateSpec extends FlatSpec with Matchers {
  val userRepository = new MockUserRepository with UserBusinessDelegate

  it should "trim leading and trailing spaces from first name search" in {
    val users = userRepository.findByFirstName(" john ")

    verifySingleUser(users)
  }

  it should "trim leading and trailing spaces from last name search" in {
    val users = userRepository.findByLastName(" doe ")

    verifySingleUser(users)
  }

  it should "trim leading and trailing spaces from first and last names search" in {
    val users = userRepository.findByFirstAndLastNames(" john ", " doe ")

    verifySingleUser(users)
  }

  it should "camel case results of first name search" in {
    val users = userRepository.findByFirstName("john")

    verifySingleUser(users)
  }

  it should "camel case results of last name search" in {
    val users = userRepository.findByLastName("doe")

    verifySingleUser(users)
  }

  it should "camel case results of first and last names search" in {
    val users = userRepository.findByFirstAndLastNames(" john ", " doe ")

    verifySingleUser(users)
  }

  it should "prettify phone number" in {
    userRepository.prettifyPhoneNum("1111111111") shouldBe ("111-111-1111")
  }

  it should "cleanse phone number from dashes" in {
    userRepository.cleansePhoneNum("111-111-1111") shouldBe ("1111111111")
  }

  it should "cleanse phone number from periods" in {
    userRepository.cleansePhoneNum("111.111.1111") shouldBe ("1111111111")
  }

  it should "reject phone number if not 10 digits long" in {
    an[IllegalArgumentException] should be thrownBy userRepository.cleansePhoneNum("111")
  }

  it should "cleanse data from leading and trailing spaces" in {
    userRepository.cleanse("   abc   ") shouldBe ("abc")
  }

  it should "convert data to lowercase" in {
    userRepository.cleanse("AbC") shouldBe ("abc")
  }

  it should "reject data if null" in {
    an[IllegalArgumentException] should be thrownBy userRepository.cleanse(null)
  }

  it should "reject data if empty" in {
    an[IllegalArgumentException] should be thrownBy userRepository.cleanse("")
  }
}