package name.abhijitsarkar.user.repository

import name.abhijitsarkar.user.domain.User
import slick.lifted.TableQuery
import scala.slick.driver.MySQLDriver.simple._
import slick.jdbc.JdbcBackend.DatabaseDef

class SlickUserRepository(private val db: DatabaseDef) extends UserRepository {
  val users = TableQuery[UserTable]
  
  override def findByFirstName(firstName: String): Seq[User] = { implicit session: Session =>
    val query = for (u <- users) yield u
    val a = query.
    val f: Future[Seq[String]] = db.run(a)
  }

  override def findByLastName(lastName: String): Seq[User] = {
    throw new UnsupportedOperationException("Implement me!")
  }

  override def findByFirstAndLastNames(firstName: String, lastName: String): Seq[User] = {
    throw new UnsupportedOperationException("Implement me!")
  }

  override def updateUser(user: User): Option[User] = {
    throw new UnsupportedOperationException("Implement me!")
  }

  override def createUser(user: User): Option[User] = {
    throw new UnsupportedOperationException("Implement me!")
  }

  override def deleteUser(userId: String): Option[User] = {
    throw new UnsupportedOperationException("Implement me!")
  }
}