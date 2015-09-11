package name.abhijitsarkar.user.repository

import scala.slick.driver.MySQLDriver.backend.Database

trait DBProvider {
  val db = Database.forConfig("mySQL")
}