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

  abstract override def findByFirstAndLastName(firstName: String, lastName: String) = {
    super.findByFirstAndLastName(cleanse(firstName), cleanse(lastName)).map { prettifyUser(_) }
  }

  abstract override def createUser(user: User) = {
    super.createUser(cleanseUser(user))
  }

  abstract override def updateUser(user: User) = {
    super.updateUser(cleanseUser(user))
  }

  abstract override def deleteUser(userId: String) = {
    super.deleteUser(userId)
  }

  def isNotNullOrEmpty(input: String) = {
    input != null && !input.trim.isEmpty
  }

  private def prettifyUser(user: User) = {
    val firstName = user.firstName.capitalize
    val lastName = user.lastName.capitalize
    val phoneNum = prettifyPhoneNum(user.phoneNum)

    new User(user.userId, firstName, lastName, phoneNum, user.email)
  }

  private def prettifyPhoneNum(phoneNum: String) = {
    val (areaCodeAndPrefix, lineNum) = phoneNum.splitAt(6)

    (areaCodeAndPrefix.splitAt(3).productIterator).mkString("-") + "-" + lineNum
  }

  private def cleanseUser(user: User) = {
    val userId = if (isNotNullOrEmpty(user.userId)) user.userId else UUID.randomUUID.toString
    val firstName = cleanse(user.firstName)
    val lastName = cleanse(user.lastName)
    val phoneNum = cleansePhoneNum(user.phoneNum)

    new User(userId, firstName, lastName, phoneNum, user.email.map { cleanse(_) })
  }

  private def cleanse(data: String) = {
    require(isNotNullOrEmpty(data), "Null or empty data.")

    data.trim.toLowerCase
  }

  private def cleansePhoneNum(phoneNum: String) = {
    val phone = cleanse(phoneNum).filterNot { c => (c == '-') || (c == '.') }

    require(phone.size == 10, "Phone number must be 10 digits.")

    phone
  }
}