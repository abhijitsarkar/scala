package name.abhijitsarkar.user.repository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection._
import name.abhijitsarkar.user.domain.User
import slick.driver.MySQLDriver.api.DBIO
import slick.driver.MySQLDriver.api.Database
import slick.driver.MySQLDriver.api.TableQuery
import slick.driver.MySQLDriver.api.columnExtensionMethods
import slick.driver.MySQLDriver.api.queryDeleteActionExtensionMethods
import slick.driver.MySQLDriver.api.queryInsertActionExtensionMethods
import slick.driver.MySQLDriver.api.queryUpdateActionExtensionMethods
import slick.driver.MySQLDriver.api.streamableQueryActionExtensionMethods
import slick.driver.MySQLDriver.api.stringColumnType
import slick.driver.MySQLDriver.api.valueToConstColumn
import slick.lifted.Query
import name.abhijitsarkar.user.service.UserService

// TODO: Compile queries
class MySQLUserRepository(private val db: Database) extends UserService {
  val users = TableQuery[Users]

  def findByFirstName(firstName: String): Future[immutable.Seq[User]] = {
    val query = users.filter { _.firstName === firstName }

    runAndThenCleanUp(query)
  }

  private def runAndThenCleanUp(query: Query[Users, User, Seq]): Future[immutable.Seq[User]] = {
    try db.run(query.result).map { _.toList } finally db.close
  }

  def findByLastName(lastName: String): Future[immutable.Seq[User]] = {
    val query = users.filter { _.lastName === lastName }

    runAndThenCleanUp(query)
  }

  def findByFirstAndLastNames(firstName: String, lastName: String): Future[immutable.Seq[User]] = {
    val query = users.filter { _.firstName === firstName }.filter { _.lastName === lastName }

    runAndThenCleanUp(query)
  }

  def updateUser(user: User) = {
    val f = (query: Query[Users, User, Seq]) => db.run(query.update(user))

    update(user.userId.get, f)
  }

  private def update(userId: String, f: (Query[Users, User, Seq]) => Future[Int]) = {
    val query = users.filter { _.userId === userId }

    try {
      db.run(query.result).flatMap {
        _ match {
          case Seq(user) => f.apply(query) map { _ => Some(user) }
          case _ => Future(None)
        }
      }
    } finally db.close
  }

  def createUser(user: User) = {
    val createAction: DBIO[Option[Int]] = users ++= Seq(user)

    try db.run(createAction.map { _.map { y => user } }) finally db.close
  }

  def deleteUser(userId: String) = {
    val f = (query: Query[Users, User, Seq]) => db.run(query.delete)

    update(userId, f)
  }
}