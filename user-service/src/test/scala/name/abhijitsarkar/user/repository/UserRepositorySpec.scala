package name.abhijitsarkar.user.repository

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.Matchers
import org.scalatest.fixture
import name.abhijitsarkar.user.TestUtil._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.Seq
import name.abhijitsarkar.user.domain.User
import org.scalatest.time.{ Millis, Seconds, Span }

trait UserRepositorySpec extends fixture.FlatSpec with Matchers with ScalaFutures {
  protected val userRepository: UserRepository

  type FixtureParam = User

  // timeout: the maximum amount of time to wait for an asynchronous operation to complete before giving up and throwing TestFailedException.
  // interval: the amount of time to sleep between each check of the status of an asynchronous operation when polling
  protected implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  def withFixture(test: OneArgTest) = {
    val testUser = createTestUser

    println("Before test")
    dumpAllUsers

    try {
      println("Running test")
      withFixture(test.toNoArgTest(testUser)) // "loan" the fixture to the test
    } finally { // clean up the fixture
      println("After test")
      dumpAllUsers

      deleteAllUsers
    }
  }

  protected def createTestUser = {
    val userId = someUserId

    println(s"Creating test user with user id: $userId")
    val testUser = User(Some(userId), "John", "Doe", randomPhoneNum, Some(randomEmail))

    val newUserId = userRepository.createUser(testUser)

    newUserId.futureValue shouldBe defined

    testUser
  }

  protected def dumpAllUsers

  protected def deleteAllUsers

  protected def someUserId: String

  it should "find user with first name" in { testUser =>
    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users.futureValue)
  }

  it should "find user with last name" in { testUser =>
    val users = userRepository.findByLastName(testUser.lastName)
    verifySingleUser(users.futureValue)
  }

  it should "be able to create new user" in { testUser =>
    val randomId = Some(someUserId)

    val user = User(randomId, "test", "test", randomPhoneNum, None)

    val newUserId = userRepository.createUser(user)
    
    randomId shouldBe(newUserId.futureValue)

    val newUser = userRepository.findById(randomId.get)
    verifySingleUser(Seq(newUser.futureValue.get), "test", "test")
  }

  it should "not be able to create users with duplicate phone numbers" in { testUser =>
    val updatedUser = testUser.copy(userId = Some(someUserId), email = Some(randomEmail))

    val newUserId = userRepository.createUser(updatedUser)

    newUserId.futureValue shouldBe empty
  }

  it should "not be able to update users with duplicate phone numbers" in { testUser =>
    val randomId = Some(someUserId)
    
    val user = testUser.copy(userId = randomId, phoneNum = randomPhoneNum, email = Some(randomEmail))

    val newUserId = userRepository.createUser(user)

    newUserId.futureValue shouldBe(randomId)

    val newUser = userRepository.findById(newUserId.futureValue.get)

    val anotherUser = newUser.futureValue.get.copy(userId = Some(someUserId), email = Some(randomEmail))

    val updatedUserId = userRepository.updateUser(anotherUser)

    updatedUserId.futureValue shouldBe empty
  }

  it should "not be able to create users with duplicate emails" in { testUser =>
    val user = testUser.copy(userId = Some(someUserId), phoneNum = randomPhoneNum)

    val newUserId = userRepository.createUser(user)

    newUserId.futureValue shouldBe empty
  }

  it should "not be able to update users with duplicate emails" in { testUser =>
    val userId = Some(someUserId)
    val user = testUser.copy(userId = userId, phoneNum = randomPhoneNum, email = Some(randomEmail))

    val newUserId = userRepository.createUser(user)

    newUserId.futureValue shouldBe(userId)

    val newUser = userRepository.findById(newUserId.futureValue.get)

    val anotherUser = newUser.futureValue.get.copy(userId = Some(someUserId), phoneNum = randomPhoneNum)

    val updatedUserId = userRepository.updateUser(anotherUser)

    updatedUserId.futureValue shouldBe empty
  }

  it should "be able to update user's email" in { testUser =>
    val email = Some(randomEmail)
    
    println(s"Updating email from: ${testUser.email.get} to: ${email.get} for user id: ${testUser.userId.get}")

    val user = testUser.copy(email = email)

    val updatedUserId = userRepository.updateUser(user)

    updatedUserId.futureValue shouldBe (testUser.userId)

    val updatedUser = userRepository.findById(updatedUserId.futureValue.get)

    def combineIdAndMail(id: Option[String], mail: Option[String]) = {
      for { userId <- id; m <- mail } yield (userId, m)
    }

    updatedUser.futureValue.flatMap { u => combineIdAndMail(u.userId, u.email) } shouldBe (combineIdAndMail(testUser.userId, email))
  }

  it should "not be able to update non existing user" in { testUser =>
    val userId = someUserId
    val user = testUser.copy(Some(userId))
    
    userRepository.findById(userId).futureValue shouldBe empty

    val updatedUserId = userRepository.updateUser(user)

    updatedUserId.futureValue shouldBe empty
  }

  it should "be able to delete user" in { testUser =>
    val deletedUserId = userRepository.deleteUser(testUser.userId.get)

    deletedUserId.futureValue shouldBe defined
  }

  it should "not be able to delete non existing user" in { testUser =>
    val deletedUserId = userRepository.deleteUser(someUserId)

    deletedUserId.futureValue shouldBe empty
  }
}