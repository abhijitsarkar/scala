package name.abhijitsarkar.user.repository

import name.abhijitsarkar.user.domain.User

trait UserRepository {
  def findByFirstName(firstName: String): Seq[User]

  def findByLastName(lastName: String): Seq[User]
  
  def findByFirstAndLastNames(firstName: String, lastName: String): Seq[User]

  def createUser(user: User): Option[User]
  
  def updateUser(user: User): Option[User]
  
  def deleteUser(userId: String): Option[User]
}
