package name.abhijitsarkar.user

import com.mongodb.DBObject
import com.mongodb.casbah.Imports.MongoClient
import com.mongodb.casbah.Imports.MongoDBObject

import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.domain.UserAttributes.ACTIVE;
import name.abhijitsarkar.user.domain.UserAttributes.EMAIL;
import name.abhijitsarkar.user.domain.UserAttributes.FIRST_NAME;
import name.abhijitsarkar.user.domain.UserAttributes.LAST_NAME;
import name.abhijitsarkar.user.domain.UserAttributes.PHONE_NUM;

import com.mongodb.casbah.MongoCollection
import java.util.UUID
import com.mongodb.casbah.WriteConcern
import com.mongodb.WriteResult

class MongoDBUserRepository(private val collection: MongoCollection) extends UserRepository {
  import MongoDBUserRepository._
  
  override def findByFirstName(firstName: String) = {
    val query = MongoDBObject(FIRST_NAME.toString -> firstName)

    findAllActiveUsers(query)
  }

  override def findByLastName(lastName: String) = {
    val query = MongoDBObject(LAST_NAME.toString -> lastName)

    findAllActiveUsers(query)
  }
  
  override def findByFirstAndLastName(firstName: String, lastName: String) = {
    val query = MongoDBObject(FIRST_NAME.toString -> firstName, LAST_NAME.toString -> lastName)

    findAllActiveUsers(query)
  }

  private def findAllActiveUsers(query: DBObject) = {
    query.put(ACTIVE.toString, true)

    collection.find(query).map {
      MongoDBUserRepository.dbObjToUser(_)
    }.toSeq
  }

  override def updateUser(user: User) = {
    val query = MongoDBObject(USER_ID -> user.userId)
    val dbObj = MongoDBUserRepository.userToDbObj(user)

    val result = collection.update(query, dbObj, false, false, WriteConcern.Safe)
    
    userIdOrNone(result, user.userId)
  }
  
  private def userIdOrNone(result: WriteResult, userId: String) = {
    if (result.getN == 1) Some(userId) else None
  }

  override def createUser(user: User) = {
    val dbObj = MongoDBUserRepository.userToDbObj(user)
    val result = collection.save(dbObj, WriteConcern.Safe)

    userIdOrNone(result, user.userId)
  }

  override def deleteUser(userId: String) = {
    val query = MongoDBObject(USER_ID -> userId)
    val dbObj = MongoDBObject(ACTIVE.toString -> false)

    val result = collection.update(query, dbObj, false, false, WriteConcern.Safe)
    
    userIdOrNone(result, userId)
  }
}

object MongoDBUserRepository {
  private val USER_ID = "_id"

  def apply(collection: MongoCollection) = {
    new MongoDBUserRepository(collection)
  }

  private[user] def dbObjToUser(obj: DBObject) = {
    val userId = obj.get(USER_ID).toString
    val firstName = obj.get(FIRST_NAME.toString).toString
    val lastName = obj.get(LAST_NAME.toString).toString
    val phoneNum = obj.get(PHONE_NUM.toString).toString
    val email = obj.get(EMAIL.toString) match {
      case x if x != null => Some(x.toString)
      case x => None
    }
    val active = obj.get(ACTIVE.toString).toString.toBoolean

    User(userId, firstName, lastName, phoneNum, email, active)
  }

  private[user] def userToDbObj(user: User) = {
    val builder = MongoDBObject.newBuilder

    builder += (USER_ID -> user.userId,
      FIRST_NAME.toString -> user.firstName,
      LAST_NAME.toString -> user.lastName,
      PHONE_NUM.toString -> user.phoneNum,
      ACTIVE.toString -> user.active)

    user.email match {
      case Some(email) => builder += EMAIL.toString -> user.email.get
      case None =>
    }

    builder.result
  }
}