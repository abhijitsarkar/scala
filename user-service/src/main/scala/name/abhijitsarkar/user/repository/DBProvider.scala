package name.abhijitsarkar.user.repository

import com.typesafe.config.ConfigFactory
import scala.slick.driver.MySQLDriver.backend.Database
import org.apache.tomcat.jdbc.pool.DataSource

trait DBProvider {
  private val dataSource = {
    val dbConfig = ConfigFactory.load().getConfig("db")

    Class.forName(dbConfig.getString("driver"))

    val ds = new DataSource()
    ds.setUrl(dbConfig.getString("jdbc.url"))
    ds.setUsername(dbConfig.getString("user"))
    ds.setPassword(dbConfig.getString("password"))

    ds
  }

  val db = Database.forDataSource(dataSource)
}