package name.abhijitsarkar.user.repository

import slick.driver.MySQLDriver.simple._
import name.abhijitsarkar.user.domain.User
import slick.lifted.ProvenShape

class UserTable(tag: Tag) extends Table[User](tag, "USER") {
  def userId = column[String]("USER_ID", O.PrimaryKey, O.AutoInc)
  def firstName = column[String]("FIRST_NAME")
  def lastName = column[String]("LAST_NAME")
  def phoneNum = column[String]("PHONE_NUM")
  def email = column[String]("EMAIL")
  
  def * = (userId.?, firstName, lastName, phoneNum, email.?) <> (User.tupled, User.unapply)
}