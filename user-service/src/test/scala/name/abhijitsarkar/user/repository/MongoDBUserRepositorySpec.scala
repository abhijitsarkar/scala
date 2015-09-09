package name.abhijitsarkar.user

import org.scalatest.Matchers
import org.scalatest.fixture
import name.abhijitsarkar.user.repository.MongoDBCollectionFactory.newCollection
import TestUtil.verifySingleUser
import name.abhijitsarkar.user.domain.User
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.BeforeAndAfterAll
import name.abhijitsarkar.user.repository.MongoDBCollectionFactory
import name.abhijitsarkar.user.repository.MongoDBUserRepository

class MongoDBUserRepositorySpec extends fixture.FlatSpec with Matchers with BeforeAndAfterAll {
  private val collection = newCollection("test")

  private val userRepository = MongoDBUserRepository(collection)

  type FixtureParam = User

  def withFixture(test: OneArgTest) = {
    val users = userRepository.findByFirstName("John")

    users shouldBe empty

    val testUser = User(None, "John", "Doe", "111-111-1111", None)

    val newUser = userRepository.createUser(testUser)

    newUser shouldBe defined

    println("Before test")
    dumpAllDocs

    try {
      println("Running test")
      withFixture(test.toNoArgTest(newUser.get)) // "loan" the fixture to the test
    } finally { // clean up the fixture
      println("After test")
      dumpAllDocs

      collection.remove(MongoDBObject.empty)
    }
  }

  override def afterAll() {
    println("Cleaning up")
    collection.drop
  }

  private def dumpAllDocs = {
    collection.find().foreach { dbObj => println(dbObj.toMap) }
  }

  it should "find user with first name" in { testUser =>
    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users)
  }

  it should "find user with last name" in { testUser =>
    val users = userRepository.findByLastName(testUser.lastName)
    verifySingleUser(users)
  }

  it should "be able to create new user" in { testUser =>
    val user = User(None, "test", "test", "555-555-9999", None)

    val newUser = userRepository.createUser(user)

    val users = userRepository.findByFirstName("test")
    verifySingleUser(users, "test", "test")

    val deletedUser = userRepository.deleteUser(newUser.get.userId.get)

    deletedUser.get.userId == newUser.get.userId
  }

  it should "not be able to create users with duplicate phone numbers" in { testUser =>
    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users)

    val user = users.head

    val updatedUser = testUser.copy(phoneNum = user.phoneNum)

    val newUser = userRepository.createUser(updatedUser)

    newUser shouldBe empty
  }

  it should "not be able to update users with duplicate phone numbers" in { testUser =>
    val user = testUser.copy(userId = Some(new ObjectId().toString), phoneNum = "222-222-2222")

    val newUser = userRepository.createUser(user)

    newUser shouldBe defined

    val anotherUser = testUser.copy(phoneNum = "222-222-2222")

    val updatedUser = userRepository.updateUser(anotherUser)

    updatedUser shouldBe empty

    userRepository.deleteUser(newUser.get.userId.get) shouldBe defined
  }

  it should "not be able to create users with duplicate emails" in { testUser =>
    val user = testUser.copy(userId = Some(new ObjectId().toString), email = Some("abc@gmail.com"), phoneNum = "222-222-2222")

    val newUser = userRepository.createUser(user)

    newUser shouldBe defined

    val anotherUser = newUser.get.copy(phoneNum = "333-333-3333")

    userRepository.createUser(anotherUser) shouldBe empty

    userRepository.deleteUser(newUser.get.userId.get) shouldBe defined
  }

  it should "not be able to update users with duplicate emails" in { testUser =>
    val user = testUser.copy(userId = Some(new ObjectId().toString), email = Some("abc@gmail.com"), phoneNum = "222-222-2222")

    val newUser = userRepository.createUser(user)

    newUser shouldBe defined

    val anotherUser = testUser.copy(email = Some("abc@gmail.com"))

    val updatedUser = userRepository.updateUser(anotherUser)

    updatedUser shouldBe empty

    userRepository.deleteUser(newUser.get.userId.get) shouldBe defined
  }

  it should "be able to update user's email" in { testUser =>
    val user = testUser.copy(email = Some("test@gmail.com"))

    val updatedUser = userRepository.updateUser(user)

    updatedUser shouldBe defined

    val users = userRepository.findByFirstName(testUser.firstName)
    verifySingleUser(users)

    users.head.email should contain("test@gmail.com")
  }

  it should "not be able to update non existing user" in { testUser =>
    val user = new User(Some(new ObjectId().toString), "test", "test", "555-555-5555", None)

    val userId = userRepository.updateUser(user)

    userId shouldBe empty
  }

  it should "be able to delete user" in { testUser =>
    val deletedUser = userRepository.deleteUser(testUser.userId.get)

    deletedUser shouldBe defined
  }

  it should "not be able to delete non existing user" in { testUser =>
    val deletedUser = userRepository.deleteUser(new ObjectId().toString)

    deletedUser shouldBe empty
  }
}