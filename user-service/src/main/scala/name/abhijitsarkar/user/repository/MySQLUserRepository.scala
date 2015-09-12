package name.abhijitsarkar.user.repository

import scala.concurrent.{ ExecutionContextExecutor, Future }
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
import scala.util.Success
import scala.util.Failure
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write
import slick.dbio.NoStream
import org.slf4j.LoggerFactory

// TODO: Compile queries
class MySQLUserRepository(private val db: Database)(private implicit val executor: ExecutionContextExecutor) extends UserService {
  val logger = LoggerFactory.getLogger(getClass)

  val users = TableQuery[Users]

  def findByFirstName(firstName: String): Future[immutable.Seq[User]] = {
    val query = users.filter { _.firstName === firstName }

    run(query)
  }

  private def run(query: Query[Users, User, Seq]): Future[immutable.Seq[User]] = {
    db.run(query.result).map { _.toList }
  }

  def findByLastName(lastName: String): Future[immutable.Seq[User]] = {
    val query = users.filter { _.lastName === lastName }

    run(query)
  }

  def findByFirstAndLastNames(firstName: String, lastName: String): Future[immutable.Seq[User]] = {
    val query = users.filter { _.firstName === firstName }.filter { _.lastName === lastName }

    run(query)
  }

  def updateUser(user: User) = {
    val f = (query: Query[Users, User, Seq]) => query.update(user)

    updateOrDelete(user.userId.get, f)
  }

  private def updateOrDelete(userId: String,
    updateOrDeleteAction: (Query[Users, User, Seq]) => DBIOAction[Int, NoStream, Write]) = {
    val query = users.filter { _.userId === userId }

    db.run(query.result.asTry).map { // Try[Seq[User]]
      case Success(users) => users match { // Seq[User]
        case Seq(user, rest @ _*) =>
          logger.debug("Found user with user id: {}.", userId); Some(user)
        case _ => logger.warn("No user with user id: {} found for update or delete.", userId); None
      }
      case Failure(ex) => logger.error(s"Failed to find user with user id: $userId", ex); None
    }.flatMap { user => // Option[User]
      user match {
        case Some(u) => db.run(updateOrDeleteAction.apply(query).asTry).map {
          case Success(numUsersInserted) => user
          case Failure(ex) => logger.error(s"Failed to update or delete user with user id: ${u.userId}", ex); None
        }
        case _ => Future.successful(None)
      }
    }
  }

  def createUser(user: User) = {
    val createAction: DBIO[Option[Int]] = users ++= immutable.Seq(user)

    db.run(createAction.asTry).map {
      case Success(numUsersInserted) => numUsersInserted.map { _ => user }
      case Failure(ex) => logger.error(s"Failed to create user with user id: ${user.userId}", ex); None
    }
  }

  def deleteUser(userId: String) = {
    val f = (query: Query[Users, User, Seq]) => query.delete

    updateOrDelete(userId, f)
  }
}