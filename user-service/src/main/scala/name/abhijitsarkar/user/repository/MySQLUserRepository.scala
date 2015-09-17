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
import scala.util.Success
import scala.util.Failure
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write
import slick.dbio.NoStream
import org.slf4j.LoggerFactory

// TODO: Compile queries
class MySQLUserRepository(private val db: Database)(private implicit val executor: ExecutionContextExecutor) extends UserRepository {
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

  def findById(userId: String): Future[Option[User]] = {
    val query = users.filter { _.userId === userId }

    db.run(query.result.asTry).map { // Try[Seq[User]]
      case Success(users) => users match { // Seq[User]
        case immutable.Seq(user) =>
          logger.debug("Found user with user id: {}.", userId); Some(user)
        case _ => logger.warn("No user found with user id: {}.", userId); None
      }
      case Failure(ex) => logger.error(s"Failed to find user with user id: $userId", ex); None
    }
  }

  def updateUser(user: User) = {
    val f = (query: Query[Users, User, Seq]) => query.update(user)

    updateOrDelete(user.userId.get, f)
  }

  private def updateOrDelete(userId: String,
    updateOrDeleteAction: (Query[Users, User, Seq]) => DBIOAction[Int, NoStream, Write]) = {
    val query = users.filter { _.userId === userId }

    findById(userId).flatMap { user => // Option[User] based on findById return type
      user match {
        case Some(u) => db.run(updateOrDeleteAction.apply(query).asTry).map {
          case Success(numUsersInserted) => {
            logger.debug("Successfully updated user with user id: {}.", u.userId.get)
            u.userId
          }
          case Failure(ex) => logger.error(s"Failed to update or delete user with user id: $userId", ex); None
        }
        case _ => Future.successful(None)
      }
    }
  }

  def createUser(user: User) = {
    val createAction = users += user

    db.run(createAction.asTry).map {
      case Success(numUsersInserted) => {
        logger.debug("Successfully created user with user id: {}.", user.userId.get);
        Some(user.userId.get)
      }
      case Failure(ex) => logger.error(s"Failed to create user with user id: ${user.userId.getOrElse("")}", ex); None
    }
  }

  def deleteUser(userId: String) = {
    val f = (query: Query[Users, User, Seq]) => query.delete

    updateOrDelete(userId, f)
  }
}

object MySQLUserRepository {
  def apply(db: Database)(implicit executor: ExecutionContextExecutor) = new MySQLUserRepository(db)(executor)
}