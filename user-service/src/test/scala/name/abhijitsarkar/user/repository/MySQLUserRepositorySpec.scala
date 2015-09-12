package name.abhijitsarkar.user.repository

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.fixture
import name.abhijitsarkar.user.TestUtil.verifySingleUser
import name.abhijitsarkar.user.repository.DBProvider.db
import slick.driver.MySQLDriver.api.TableQuery
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.Seq
import name.abhijitsarkar.user.domain.User
import slick.driver.MySQLDriver.api._
import scala.concurrent.duration.Duration
import org.scalatest.time.{ Millis, Seconds, Span }
import scala.util.Random

class MySQLUserRepositorySpec extends fixture.FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {
  private val userRepository = new MySQLUserRepository(db)(global)
  private val randomGenerator = new Random
  
  private def randomUserId = randomGenerator.nextInt(Short.MaxValue).toString
  
  implicit val defaultPatience = PatienceConfig(timeout = Span(2, Seconds), interval = Span(500, Millis))

  val query = TableQuery[Users]

  type FixtureParam = User

  def withFixture(test: OneArgTest) = {
    val users = userRepository.findByFirstName("John")

    users.futureValue shouldBe empty

    val testUser = User(Some("1"), "John", "Doe", "111-111-1111", None)

    val newUser = userRepository.createUser(testUser)

    val user = newUser.futureValue
    user shouldBe defined

    println("Before test")
    dumpAllUsers

    try {
      println("Running test")
      withFixture(test.toNoArgTest(user.get)) // "loan" the fixture to the test
    } finally { // clean up the fixture
      println("After test")
      dumpAllUsers

      db.run(query.delete)
    }
  }

  override def afterAll() {
    println("Cleaning up")
    try db.run(query.delete) finally db.close
  }

  private def dumpAllUsers = {
    println("Printing all users")
    db.run(query.result).map { println(_) }
  }

  it should "find user with first name" in { testUser =>
    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users.futureValue)
  }

  it should "find user with last name" in { testUser =>
    val users = userRepository.findByLastName(testUser.lastName)
    verifySingleUser(users.futureValue)
  }

  it should "be able to create new user" in { testUser =>
    val user = User(Some(randomUserId), "test", "test", "555-555-9999", None)

    val newUser = userRepository.createUser(user)

    val users = userRepository.findByFirstName("test")
    verifySingleUser(users.futureValue, "test", "test")

    val deletedUser = userRepository.deleteUser(newUser.futureValue.get.userId.get)

    deletedUser.futureValue.get.userId == newUser.futureValue.get.userId
  }

  it should "not be able to create users with duplicate phone numbers" in { testUser =>
    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users.futureValue)

    val user = users.futureValue.head

    val updatedUser = testUser.copy(phoneNum = user.phoneNum)

    val newUser = userRepository.createUser(updatedUser)

    newUser.futureValue shouldBe empty
  }

  it should "not be able to update users with duplicate phone numbers" in { testUser =>
    val user = testUser.copy(userId = Some(randomUserId), phoneNum = "222-222-2222")

    val newUser = userRepository.createUser(user)

    newUser.futureValue shouldBe defined

    val anotherUser = testUser.copy(phoneNum = "222-222-2222")

    val updatedUser = userRepository.updateUser(anotherUser)

    updatedUser.futureValue shouldBe empty

    userRepository.deleteUser(newUser.futureValue.get.userId.get).futureValue shouldBe defined
  }

  it should "not be able to create users with duplicate emails" in { testUser =>
    val user = testUser.copy(userId = Some(randomUserId), email = Some("abc@gmail.com"), phoneNum = "222-222-2222")

    val newUser = userRepository.createUser(user)

    newUser.futureValue shouldBe defined

    val anotherUser = newUser.futureValue.get.copy(phoneNum = "333-333-3333")

    userRepository.createUser(anotherUser).futureValue shouldBe empty

    userRepository.deleteUser(newUser.futureValue.get.userId.get).futureValue shouldBe defined
  }

  it should "not be able to update users with duplicate emails" in { testUser =>
    val user = testUser.copy(userId = Some(randomUserId), email = Some("abc@gmail.com"), phoneNum = "222-222-2222")

    val newUser = userRepository.createUser(user)

    newUser.futureValue shouldBe defined

    val anotherUser = testUser.copy(email = Some("abc@gmail.com"))

    val updatedUser = userRepository.updateUser(anotherUser)

    updatedUser.futureValue shouldBe empty

    userRepository.deleteUser(newUser.futureValue.get.userId.get).futureValue shouldBe defined
  }

  it should "be able to update user's email" in { testUser =>
    val user = testUser.copy(email = Some("test@gmail.com"))

    val updatedUser = userRepository.updateUser(user)

    updatedUser.futureValue shouldBe defined

    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users.futureValue)

    users.futureValue.head.email should contain("test@gmail.com")
  }

  it should "not be able to update non existing user" in { testUser =>
    val user = new User(Some(randomUserId), "test", "test", "555-555-5555", None)

    val updatedUser = userRepository.updateUser(user)

    updatedUser.futureValue shouldBe empty
  }

  it should "be able to delete user" in { testUser =>
    val deletedUser = userRepository.deleteUser(testUser.userId.get)

    deletedUser.futureValue shouldBe defined
  }

  it should "not be able to delete non existing user" in { testUser =>
    val deletedUser = userRepository.deleteUser(randomUserId)

    deletedUser.futureValue shouldBe empty
  }
}