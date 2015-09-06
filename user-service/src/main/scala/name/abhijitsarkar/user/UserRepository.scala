package name.abhijitsarkar.user

import name.abhijitsarkar.user.domain.User
import com.mongodb.casbah.MongoClient

trait UserRepository {
  def findByFirstName(firstName: String): Seq[User]

  def findByLastName(lastName: String): Seq[User]
  
  def findByFirstAndLastName(firstName: String, lastName: String): Seq[User]

  def createUser(user: User): Option[String]
  
  def updateUser(userId: String, user: User): Option[String]
  
  def deleteUser(userId: String): Option[String]
}
