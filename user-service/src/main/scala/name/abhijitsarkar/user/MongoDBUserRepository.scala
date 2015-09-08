package name.abhijitsarkar.user

import com.mongodb.DBObject
import com.mongodb.casbah.Imports.MongoClient
import com.mongodb.casbah.Imports.MongoDBObject
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.domain.UserAttributes.ACTIVE
import name.abhijitsarkar.user.domain.UserAttributes.EMAIL
import name.abhijitsarkar.user.domain.UserAttributes.FIRST_NAME
import name.abhijitsarkar.user.domain.UserAttributes.LAST_NAME
import name.abhijitsarkar.user.domain.UserAttributes.PHONE_NUM
import com.mongodb.casbah.MongoCollection
import java.util.UUID
import com.mongodb.casbah.WriteConcern
import com.mongodb.WriteResult
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.slf4j.LoggerFactory
import org.bson.types.ObjectId

class MongoDBUserRepository(private val collection: MongoCollection) extends UserRepository {
  import MongoDBUserRepository._

  override def findByFirstName(firstName: String) = {
    val query = MongoDBObject(FIRST_NAME.toString -> firstName)

    findAllMatchingUsers(query)
  }

  override def findByLastName(lastName: String) = {
    val query = MongoDBObject(LAST_NAME.toString -> lastName)

    findAllMatchingUsers(query)
  }

  override def findByFirstAndLastName(firstName: String, lastName: String) = {
    val query = MongoDBObject(FIRST_NAME.toString -> firstName, LAST_NAME.toString -> lastName)

    findAllMatchingUsers(query)
  }

  private def findAllMatchingUsers(query: DBObject) = {
    collection.find(query).map { dbObjToUser(_) }.toSeq
  }

  override def updateUser(user: User) = {
    val query = MongoDBObject(USER_ID -> new ObjectId(user.userId.get))
    val dbObj = userToDbObj(user)
    
    val result = Try(collection.findAndModify(query = query, update = dbObj))

    processResult(result)
  }

  private def processResult(result: Try[Option[collection.T]]) = {
    result match {
      case Success(Some(dbObj)) => Some(dbObj).map { dbObjToUser(_) }
      case Success(None) => logger.info(s"Didn't find user to update or delete."); None
      case Failure(ex) => logger.error("Failed to update or delete user.", ex); None
    }
  }

  override def createUser(user: User) = {
    val dbObj = userToDbObj(user)

    val result = Try(collection.insert(dbObj, WriteConcern.Safe))

    result match {
      case Success(writeResult) if (writeResult.getN == 1) => Some(user)
      case Success(writeResult) =>
        logger.error(s"Failed to create user: ${dbObj.toMap}, write result: ${writeResult}."); None
      case Failure(ex) => logger.error("Failed to create user.", ex); None
    }
  }

  override def deleteUser(userId: String) = {
    val query = MongoDBObject(USER_ID -> new ObjectId(userId))
    
    val result = Try(collection.findAndRemove(query))
    
    processResult(result)
  }
}

object MongoDBUserRepository {
  private val logger = LoggerFactory.getLogger(getClass)

  val USER_ID = "_id"

  def apply(collection: MongoCollection) = {
    new MongoDBUserRepository(collection)
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

    builder += (USER_ID -> (user.userId match {
      case Some(userId) if (ObjectId.isValid(userId)) => logger.info("Using given user id."); new ObjectId(userId)
      case _ => logger.info("Generating new user id."); new ObjectId()
    }))

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