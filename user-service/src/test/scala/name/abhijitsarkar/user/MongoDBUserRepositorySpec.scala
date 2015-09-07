package name.abhijitsarkar.user

import scala.io.Source

import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers
import org.scalatest.fixture

import com.mongodb.BasicDBObject
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.util.JSON

import name.abhijitsarkar.user.MongoDBUserRepository.dbObjToUser
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.domain.UserAttributes.PHONE_NUM
import MongoDBCollectionFactory.newCollection

class MongoDBUserRepositorySpec extends fixture.FlatSpec with Matchers with BeforeAndAfterAll {
  private val collection = newCollection("users")

  private val userRepository = new MongoDBUserRepository(newCollection("users"))

  private val testUser = User("1", "John", "Doe", "111-111-1111", None)

  override def afterAll() {
    println("Cleaning up")
    collection.drop
  }

  type FixtureParam = String

  def withFixture(test: OneArgTest) = {
    val users = userRepository.findByFirstName("test")

    users shouldBe empty

    val newUserId = userRepository.createUser(testUser)

    newUserId shouldBe defined

    println("Before test")
    dumpAllDocs

    try {
      withFixture(test.toNoArgTest(newUserId.get)) // "loan" the fixture to the test
    } finally { // clean up the fixture
      println("After test")
      dumpAllDocs

      collection.remove(MongoDBObject())
    }
  }

  private def dumpAllDocs = {
    collection.find().foreach { dbObj => println(dbObj.toMap) }
  }

  "We" should "find user with first name" in { userId =>
    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users)
  }

  private def verifySingleUser(users: Seq[User], expectedFirstName: String = testUser.firstName, expectedLastName: String = testUser.lastName) {
    users should have size (1)

    val user = users.head

    user.firstName shouldBe (expectedFirstName)
    user.lastName shouldBe (expectedLastName)
  }

  "We" should "find user with last name" in { userId =>
    val users = userRepository.findByLastName(testUser.lastName)
    verifySingleUser(users)
  }

  "We" should "be able to create new user" in { userId =>
    val newUser = User("", "test", "test", "555-555-9999", None)

    val newUserId = userRepository.createUser(newUser)

    val users = userRepository.findByFirstName("test")
    verifySingleUser(users, "test", "test")

    val deletedUserId = userRepository.deleteUser(newUserId.get)

    deletedUserId == newUserId
  }

  ignore should "not be able to create users with duplicate ids" in { userId =>
    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users)

    val newUserId = userRepository.createUser(testUser)

    newUserId shouldBe empty
  }

  "We" should "not be able to create users with duplicate phone numbers" in { userId =>
    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users)

    val user = users.head

    val updatedUser = testUser.copy(userId = "2", phoneNum = user.phoneNum)

    updatedUser.phoneNum == user.phoneNum

    val newUserId = userRepository.createUser(updatedUser)

    newUserId shouldBe empty
  }
  
  "We" should "not be able to update users with duplicate phone numbers" in { userId =>
    val newUser = testUser.copy(userId = "2", phoneNum = "222-222-2222")

    val newUserId = userRepository.createUser(newUser)

    newUserId should contain(newUser.userId)

    val updatedUser = testUser.copy(phoneNum = "222-222-2222")

    val updatedUserId = userRepository.updateUser(updatedUser)

    updatedUserId shouldBe empty
    
    userRepository.deleteUser(newUserId.get) == newUserId
  }

  "We" should "not be able to create users with duplicate emails" in { userId =>
    var newUser = testUser.copy(userId = "2", email = Some("abc@gmail.com"), phoneNum = "222-222-2222")

    val newUserId = userRepository.createUser(newUser)

    newUserId should contain(newUser.userId)

    newUser = newUser.copy(userId = "3", phoneNum = "333-333-3333")

    userRepository.createUser(newUser) shouldBe empty

    userRepository.deleteUser(newUserId.get) == newUserId
  }

  "We" should "not be able to update users with duplicate emails" in { userId =>
    val newUser = testUser.copy(userId = "2", email = Some("abc@gmail.com"), phoneNum = "222-222-2222")

    val newUserId = userRepository.createUser(newUser)

    newUserId should contain(newUser.userId)

    val updatedUser = testUser.copy(email = Some("abc@gmail.com"))

    val updatedUserId = userRepository.updateUser(updatedUser)

    updatedUserId shouldBe empty
    
    userRepository.deleteUser(newUserId.get) == newUserId
  }

  "We" should "be able to update user's email" in { userId =>
    val updatedUser = testUser.copy(email = Some("test@gmail.com"))

    val updatedUserId = userRepository.updateUser(updatedUser)

    updatedUserId should contain(userId)

    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users)

    users.head.email should contain("test@gmail.com")
  }

  "We" should "not be able to update non existing user" in { userId =>
    val user = new User("junk", "test", "test", "555-555-5555", None)

    val userId = userRepository.updateUser(user)

    userId shouldBe empty
  }

  "We" should "be able to delete user" in { userId =>
    val deletedUserId = userRepository.deleteUser(userId)

    deletedUserId should contain(userId)
  }

  "We" should "not be able to delete non existing user" in { userId =>
    val userId = userRepository.deleteUser("junk")

    userId shouldBe empty
  }
}