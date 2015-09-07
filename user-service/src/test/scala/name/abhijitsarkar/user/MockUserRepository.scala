package name.abhijitsarkar.user

import name.abhijitsarkar.user.domain.User

class MockUserRepository extends UserRepository {
  private val empty = ""
  private val userId = "1"

  override def findByFirstName(firstName: String): Seq[User] = {
    if (firstName.equalsIgnoreCase("John")) Seq[User](newUser(firstName, empty)) else Seq.empty[User]
  }

  override def findByLastName(lastName: String): Seq[User] = {
    if (lastName.equalsIgnoreCase("Doe")) Seq[User](newUser(empty, lastName)) else Seq.empty[User]
  }

  override def findByFirstAndLastName(firstName: String, lastName: String): Seq[User] = {
    if (firstName.equalsIgnoreCase("John") && lastName.equalsIgnoreCase("Doe"))
      Seq[User](newUser(firstName, lastName))
    else Seq.empty[User]
  }

  override def updateUser(user: User): Option[String] = {
    if (user.userId == this.userId) Some(userId) else None
  }

  override def createUser(user: User): Option[String] = {
    if (user.userId == userId) Some(userId) else None
  }

  override def deleteUser(userId: String): Option[String] = {
    if (userId == this.userId) Some(userId) else None
  }

  private def newUser(firstName: String, lastName: String) = {
    User(userId, firstName, lastName, "555-555-5555", Some("johndoe@gmail.com"), true)
  }
}
