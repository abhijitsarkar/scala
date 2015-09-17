package name.abhijitsarkar.user.repository

import name.abhijitsarkar.user.repository.DBProvider.db
import slick.driver.MySQLDriver.api.TableQuery
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._
import name.abhijitsarkar.user.TestUtil._
import org.scalatest.BeforeAndAfterAll

class MySQLUserRepositorySpec extends UserRepositorySpec with BeforeAndAfterAll {
  override protected val userRepository = MySQLUserRepository(db) { implicitly }

  val query = TableQuery[Users]

  override def afterAll() {
    println("Cleaning up")
    try db.run(query.delete) finally db.close
  }

  override protected def dumpAllUsers = {
    println("Printing all users")
    db.run(query.result).map { println(_) }
  }

  override protected def deleteAllUsers() = {
    println("Deleting all users")
    db.run(query.delete)
  }
  
  override protected def someUserId = {
    randomUserId
  }
}