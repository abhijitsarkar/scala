package name.abhijitsarkar.user.repository

import scala.collection.immutable
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import org.bson.types.ObjectId
import org.slf4j.LoggerFactory

import com.mongodb.DBObject
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject

import MongoDBUserRepository.USER_ID
import MongoDBUserRepository.dbObjToUser
import MongoDBUserRepository.logger
import MongoDBUserRepository.userToDbObj
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.domain.UserAttributes.EMAIL
import name.abhijitsarkar.user.domain.UserAttributes.FIRST_NAME
import name.abhijitsarkar.user.domain.UserAttributes.LAST_NAME
import name.abhijitsarkar.user.domain.UserAttributes.PHONE_NUM

class MongoDBUserRepository(private val collection: MongoCollection)(private implicit val materializer: Materializer)
    extends UserRepository {
  private val noSuchUser = Option.empty[User]
  private val noUsersFound = immutable.Seq.empty[User]

  override def findByFirstName(firstName: String) = {
    val query = MongoDBObject(FIRST_NAME.toString -> firstName)

    findAllMatchingUsers(query)
  }

  override def findByLastName(lastName: String) = {
    val query = MongoDBObject(LAST_NAME.toString -> lastName)

    findAllMatchingUsers(query)
  }

  override def findByFirstAndLastNames(firstName: String, lastName: String) = {
    val query = MongoDBObject(FIRST_NAME.toString -> firstName, LAST_NAME.toString -> lastName)

    findAllMatchingUsers(query)
  }

  override def findById(userId: String) = {
    val src = Source.single(collection.findOneByID(new ObjectId(userId)))

    src.map { _.map { dbObjToUser } }.runWith(Sink.head)
  }

  private def findAllMatchingUsers(query: DBObject) = {
    val src = Source(collection.find(query).toList)

    src.map { dbObjToUser }.runFold(noUsersFound)(_ :+ _)
  }

  override def updateUser(user: User) = update(user)

  private def update(user: User, createNew: Boolean = false) = {
    logger.debug("Updating user with user id: {}.", user.userId)
    
    val result = Try {
      val query = MongoDBObject(USER_ID -> new ObjectId(user.userId.get))
      val dbObj = userToDbObj(user)
      val emptyDoc = MongoDBObject()

      collection.findAndModify(query = query, fields = null, sort = null, remove = false, update = dbObj,
        returnNew = true, upsert = createNew)
    }

    processResult(result)
  }

  private def processResult(result: Try[Option[collection.T]]) = {
    val src = Source.single(result)

    src.map {
      _ match {
        case Success(x) => x.map { dbObjToUser }.map { _.userId.get }
        case Failure(ex) => logger.error("Failed to update or delete user.", ex); None
      }
    }.runWith(Sink.head)
  }

  override def createUser(user: User) = {
    update(user, true)
  }

  override def deleteUser(userId: String) = {
    val result = Try {
      val query = MongoDBObject(USER_ID -> new ObjectId(userId))
      collection.findAndRemove(query)
    }

    processResult(result)
  }
}

object MongoDBUserRepository {
  private val logger = LoggerFactory.getLogger(getClass)

  val USER_ID = "_id"

  def apply(collection: MongoCollection)(implicit materializer: Materializer) = {
    new MongoDBUserRepository(collection)(materializer)
  }

  private def dbObjToUser(obj: DBObject) = {
    val userId = obj.get(USER_ID).toString
    val firstName = obj.get(FIRST_NAME.toString).toString
    val lastName = obj.get(LAST_NAME.toString).toString
    val phoneNum = obj.get(PHONE_NUM.toString).toString
    val email = obj.get(EMAIL.toString) match {
      case x if x != null => Some(x.toString)
      case _ => None
    }

    User(Some(userId), firstName, lastName, phoneNum, email)
  }

  private def userToDbObj(user: User) = {
    val builder = MongoDBObject.newBuilder

    builder += USER_ID -> (user.userId match {
      case Some(userId) if (ObjectId.isValid(userId)) =>
        logger.info("Using given user id {}.", userId); new ObjectId(userId)
      case _ => val userId = new ObjectId(); logger.info("Generating new user id {}.", userId);
    })

    builder += (FIRST_NAME.toString -> user.firstName,
      LAST_NAME.toString -> user.lastName,
      PHONE_NUM.toString -> user.phoneNum)

    user.email match {
      case Some(email) => builder += EMAIL.toString -> user.email.get
      case _ =>
    }

    builder.result
  }
}