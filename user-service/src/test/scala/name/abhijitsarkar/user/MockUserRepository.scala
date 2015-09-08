package name.abhijitsarkar.user

import name.abhijitsarkar.user.domain.User

class MockUserRepository extends UserRepository {
  private val mockUser = User(Some("1"), "John", "Doe", "555-555-5555", Some("johndoe@gmail.com"))

  override def findByFirstName(firstName: String): Seq[User] = {
    if (firstName.equalsIgnoreCase(mockUser.firstName)) Seq[User](mockUser) else Seq.empty[User]
  }

  override def findByLastName(lastName: String): Seq[User] = {
    if (lastName.equalsIgnoreCase(mockUser.lastName)) Seq[User](mockUser) else Seq.empty[User]
  }

  override def findByFirstAndLastNames(firstName: String, lastName: String): Seq[User] = {
    if (firstName.equalsIgnoreCase(mockUser.firstName) && lastName.equalsIgnoreCase(mockUser.lastName))
      Seq[User](mockUser)
    else Seq.empty[User]
  }

  override def updateUser(user: User): Option[User] = {
    if (user.userId == mockUser.userId) Some(mockUser) else None
  }

  override def createUser(user: User): Option[User] = {
    if (user.userId == mockUser.userId) Some(mockUser) else None
  }

  override def deleteUser(userId: String): Option[User] = {
    if (userId == mockUser.userId.get) Some(mockUser) else None
  }
}
