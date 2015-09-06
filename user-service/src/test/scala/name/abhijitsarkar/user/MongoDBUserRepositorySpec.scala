package name.abhijitsarkar.user

import scala.io.Source
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.mongodb.BasicDBObject
import com.mongodb.casbah.MongoClient
import com.mongodb.util.JSON
import name.abhijitsarkar.user.MongoDBUserRepository.dbObjToUser
import name.abhijitsarkar.user.domain.User

class MongoDBUserRepositorySpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  private val collection = MongoClient()("akka")("users")
  private val userRepository = new MongoDBUserRepository(collection) with UserBusinessDelegate

  override def beforeAll = {
    println("Initializing...")
    userRepository.collection.drop()

    val users = Source.fromInputStream(getClass.getResourceAsStream("/users.json"))

    for (line <- users.getLines) {
      val userDBObj = JSON.parse(line).asInstanceOf[BasicDBObject]

      val user = dbObjToUser(userDBObj)

      userRepository.createUser(user)
    }
  }

  override def afterAll = {
    println("Cleaning up...")
    userRepository.collection.drop()
  }

  "We" should "find user with first name John" in {
    val users = userRepository.findByFirstName("John")
    verifySingleUser(users, "John", "Doe")
  }

  private def verifySingleUser(users: Seq[User], expectedFirstName: String, expectedLastName: String) {
    users.size shouldBe (1)

    val user = users.head

    user.firstName shouldBe (expectedFirstName)
    user.lastName shouldBe (expectedLastName)
  }

  "We" should "find 2 users with last name Doe" in {
    val users = userRepository.findByLastName("Doe")
    users.size shouldBe (2)
  }

  "Johnny" should "not have an email on file" in {
    val users = userRepository.findByFirstName("Johnny")
    verifySingleUser(users, "Johnny", "Appleseed")

    users.head.email shouldBe empty
  }

  "Leading or trailing spaces" should "be trimmed during search" in {
    val users = userRepository.findByFirstName(" John ")
    verifySingleUser(users, "John", "Doe")
  }

  "We" should "be able to update user's phone number" in {
    val newUser = User("", "test", "test", "555-555-5555", None)

    val newUserId = userRepository.createUser(newUser).get
    
    val updatedUser = newUser.copy(userId = newUserId).copy(phoneNum = "555-555-6666")
    
    val updatedUserId = userRepository.updateUser(newUserId, updatedUser).get
    
    updatedUserId == newUserId
    
    val users = userRepository.findByFirstName("test")
    verifySingleUser(users, "Test", "Test")
    
    users.head.phoneNum shouldBe("555-555-6666")
    
    val deletedUserId = userRepository.deleteUser(updatedUserId).get
    
    deletedUserId == updatedUserId
  }
  
  "We" should "not be able to update non existing user" in {
    val user = new User("", "test", "test", "555-555-5555", None)
    
    val userId = userRepository.updateUser("junk", user)
    
    userId shouldBe empty
  }
  
  "We" should "be able to delete user" in {
    val newUser = User("", "test", "test", "555-555-5555", None)

    val newUserId = userRepository.createUser(newUser).get
    
    val deletedUserId = userRepository.deleteUser(newUserId).get
    
    deletedUserId == newUserId
    
    val users = userRepository.findByFirstName("test")
    
    users.isEmpty == true
  }
  
  "We" should "not be able to delete non existing user" in {
    val userId = userRepository.deleteUser("junk")
    
    userId shouldBe empty
  }
}