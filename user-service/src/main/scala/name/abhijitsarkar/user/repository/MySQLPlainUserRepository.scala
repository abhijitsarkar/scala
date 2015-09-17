package name.abhijitsarkar.user.repository

import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.collection._
import name.abhijitsarkar.user.domain.User
import slick.driver.MySQLDriver.api._
import slick.jdbc.GetResult
import scala.util.Success
import scala.util.Failure
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write
import slick.dbio.NoStream
import org.slf4j.LoggerFactory

class MySQLPlainUserRepository(private val db: Database)(private implicit val executor: ExecutionContextExecutor) extends UserRepository {
  val logger = LoggerFactory.getLogger(getClass)

  val table = "users"

  implicit val getUserResult = GetResult[User](u => User(u.<<, u.<<, u.<<, u.<<, u.<<))

  def findByFirstName(firstName: String): Future[immutable.Seq[User]] = {
    val query = s"first_name = '$firstName'"

    run(query)
  }

  private def run(whereClause: String): Future[immutable.Seq[User]] = {
    val query: DBIO[Seq[User]] =
      sql"""SELECT *
      FROM #$table
      WHERE #$whereClause
      """.as[User]

    db.run(query).map { _.toList }
  }

  def findByLastName(lastName: String): Future[immutable.Seq[User]] = {
    val query = s"last_name = '$lastName'"

    run(query)
  }

  def findByFirstAndLastNames(firstName: String, lastName: String): Future[immutable.Seq[User]] = {
    val query = s"first_name = '$firstName' AND last_name = '$lastName'"

    run(query)
  }

  def findById(userId: String): Future[Option[User]] = {
    val query = s"user_id = '$userId'"

    run(query).map { // Future[Seq[User]]
      _ match { // Seq[User]
        case immutable.Seq(user) =>
          logger.debug("Found user with user id: {}.", userId); Some(user)
        case _ => logger.warn("No user found with user id: {}.", userId); None
      }
    }
  }

  def updateUser(user: User) = {
    val action = sqlu"""UPDATE #$table SET
      first_name = ${user.firstName},
      last_name = ${user.lastName},
      phone_num = ${user.phoneNum},
      email = ${user.email}
      WHERE user_id = ${user.userId}
    """

    logger.debug(s"Update statement: ${action.statements.head} for user id: ${user.userId}.")

    createUpdateOrDelete(user.userId.get, action)
  }

  private def createUpdateOrDelete(userId: String, action: DBIO[Int]) = {
    db.run(action.asTry).map {
      case Success(numUsersImpacted) if (numUsersImpacted > 0) => {
        logger.debug("Successfully created, updated or deleted user with user id: {}.", userId)
        Some(userId)
      }
      case Success(_) => None
      case Failure(ex) => logger.error(s"Failed to create, update or delete user with user id: $userId", ex); None
    }
  }

  def createUser(user: User) = {
    val action = sqlu"""INSERT INTO #$table VALUES(
      ${user.userId}, ${user.firstName}, ${user.lastName}, ${user.phoneNum}, ${user.email}
    )"""

    logger.debug(s"Insert statement: ${action.statements.head} for user id: ${user.userId}.")

    createUpdateOrDelete(user.userId.get, action)
  }

  def deleteUser(userId: String) = {
    val action = sqlu"""DELETE FROM #$table
      WHERE user_id = ${userId}
    """
    logger.debug(s"Delete statement: ${action.statements.head} for user id: ${userId}.")

    createUpdateOrDelete(userId, action)
  }
}

object MySQLPlainUserRepository {
  def apply(db: Database)(implicit executor: ExecutionContextExecutor) = new MySQLPlainUserRepository(db)(executor)
}