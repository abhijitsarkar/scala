package name.abhijitsarkar.user.repository

import slick.driver.MySQLDriver.api._
import name.abhijitsarkar.user.domain.User
import slick.lifted.ProvenShape

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def userId = column[String]("USER_ID", O.PrimaryKey)
  def firstName = column[String]("FIRST_NAME")
  def lastName = column[String]("LAST_NAME")
  def phoneNum = column[String]("PHONE_NUM")
  def email = column[String]("EMAIL")
  
  def * = (userId.?, firstName, lastName, phoneNum, email.?) <> (User.tupled, User.unapply)
}