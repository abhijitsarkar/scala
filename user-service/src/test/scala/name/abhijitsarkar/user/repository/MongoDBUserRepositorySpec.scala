package name.abhijitsarkar.user.repository

import name.abhijitsarkar.user.repository.MongoDBCollectionFactory.newCollection
import name.abhijitsarkar.user.ActorPlumbing
import com.mongodb.casbah.commons.MongoDBObject
import org.scalatest.concurrent.ScalaFutures
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.bson.types.ObjectId
import org.scalatest.BeforeAndAfterAll

class MongoDBUserRepositorySpec extends UserRepositorySpec with BeforeAndAfterAll {
  implicit val system = ActorSystem("user-service")
  implicit def executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  private val collection = newCollection("test")

  override protected val userRepository = MongoDBUserRepository(collection)(materializer)

  override def afterAll() {
    println("Cleaning up")
    collection.drop
  }

  override protected def dumpAllUsers = {
    println("Printing all users")
    collection.find().foreach { dbObj => println(dbObj.toMap) }
  }

  override protected def deleteAllUsers() = {
    collection.remove(MongoDBObject.empty)
  }

  override protected def someUserId = {
    new ObjectId().toString()
  }
}