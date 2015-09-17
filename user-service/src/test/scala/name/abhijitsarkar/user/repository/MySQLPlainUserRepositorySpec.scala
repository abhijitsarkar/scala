package name.abhijitsarkar.user.repository

import name.abhijitsarkar.user.repository.DBProvider.db
import slick.driver.MySQLDriver.api.TableQuery
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._
import name.abhijitsarkar.user.TestUtil._
import org.scalatest.BeforeAndAfterAll
import slick.jdbc.GetResult
import name.abhijitsarkar.user.domain.User

class MySQLPlainUserRepositorySpec extends UserRepositorySpec with BeforeAndAfterAll {
  override protected val userRepository = MySQLPlainUserRepository(db) { implicitly }

  val table = "users"
  implicit val getUserResult = GetResult[User](u => User(u.<<, u.<<, u.<<, u.<<, u.<<))

  override def afterAll() {
    println("Cleaning up")
    deleteAllUsers()
  }

  override protected def dumpAllUsers = {
    println("Printing all users")
    val query = sql"SELECT * FROM #$table".as[User]

    db.run(query).map { println(_) }
  }

  override protected def deleteAllUsers() = {
    println("Deleting all users")
    val action = sqlu"DELETE FROM #$table"

    db.run(action)
  }

  override protected def someUserId = {
    randomUserId
  }
}