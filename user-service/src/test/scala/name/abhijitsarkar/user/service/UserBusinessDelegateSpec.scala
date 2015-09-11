package name.abhijitsarkar.user.service

import scala.collection.immutable.Seq
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.scalatest.FlatSpec
import org.scalatest.Matchers

import akka.util.Timeout
import name.abhijitsarkar.user.TestUtil.verifySingleUser
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.repository.MockUserRepository

class UserBusinessDelegateSpec extends FlatSpec with Matchers {
  val userRepository = new MockUserRepository with UserBusinessDelegate
  
  implicit val timeout = Timeout(1 seconds)
  
  private def waitAndThenVerify(future: Future[Seq[User]]) = {
    val users = Await.result(future, timeout.duration).asInstanceOf[Seq[User]]
    
    verifySingleUser(users)
  }

  it should "trim leading and trailing spaces from first name search" in {
    val future = userRepository.findByFirstName(" john ")
    
    waitAndThenVerify(future)
  }

  it should "trim leading and trailing spaces from last name search" in {
    val future =  userRepository.findByLastName(" doe ")

    waitAndThenVerify(future)
  }

  it should "trim leading and trailing spaces from first and last names search" in {
    val future = userRepository.findByFirstAndLastNames(" john ", " doe ")

    waitAndThenVerify(future)
  }

  it should "camel case results of first name search" in {
    val future = userRepository.findByFirstName("john")

    waitAndThenVerify(future)
  }

  it should "camel case results of last name search" in {
    val future = userRepository.findByLastName("doe")

    waitAndThenVerify(future)
  }

  it should "camel case results of first and last names search" in {
    val future = userRepository.findByFirstAndLastNames(" john ", " doe ")

    waitAndThenVerify(future)
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