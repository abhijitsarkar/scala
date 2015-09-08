package name.abhijitsarkar.user

import name.abhijitsarkar.user.domain.User
import java.util.UUID

trait UserBusinessDelegate extends UserRepository {
  abstract override def findByFirstName(firstName: String) = {
    super.findByFirstName(cleanse(firstName)).map { prettifyUser(_) }
  }

  abstract override def findByLastName(lastName: String) = {
    super.findByLastName(cleanse(lastName)).map { prettifyUser(_) }
  }

  abstract override def findByFirstAndLastNames(firstName: String, lastName: String) = {
    super.findByFirstAndLastNames(cleanse(firstName), cleanse(lastName)).map { (prettifyUser(_)) }
  }

  abstract override def createUser(user: User) = {
    super.createUser(cleanseUser(user))
  }

  abstract override def updateUser(user: User) = {
    require(user.userId != None, "User id must be defined.")
    super.updateUser(cleanseUser(user))
  }

  abstract override def deleteUser(userId: String) = {
    require(userId != null, "User id must not be null.")
    super.deleteUser(userId)
  }

  def isNotNullOrEmpty(input: String) = {
    input != null && !input.trim.isEmpty
  }

  private def prettifyUser(user: User) = {
    val firstName = user.firstName.capitalize
    val lastName = user.lastName.capitalize
    val phoneNum = prettifyPhoneNum(user.phoneNum)

    user.copy(firstName = firstName, lastName = lastName, phoneNum = phoneNum)
  }

  private[user] def prettifyPhoneNum(phoneNum: String) = {
    val (areaCodeAndPrefix, lineNum) = phoneNum.splitAt(6)

    (areaCodeAndPrefix.splitAt(3).productIterator).mkString("-") + "-" + lineNum
  }

  private[user] def cleanseUser(user: User) = {
    val userId = user.userId.map { cleanse }

    val firstName = cleanse(user.firstName)
    val lastName = cleanse(user.lastName)
    val phoneNum = cleansePhoneNum(user.phoneNum)

    val email = user.email.map { cleanse(_) }

    user.copy(userId = userId, firstName = firstName, lastName = lastName, phoneNum = phoneNum,
      email = email)
  }

  private[user] def cleanse(data: String) = {
    require(isNotNullOrEmpty(data), "Null or empty data.")

    data.trim.toLowerCase
  }

  private[user] def cleansePhoneNum(phoneNum: String) = {
    val phone = cleanse(phoneNum).filterNot { c => (c == '-') || (c == '.') }

    require(phone.size == 10, "Phone number must be 10 digits.")

    phone
  }
}