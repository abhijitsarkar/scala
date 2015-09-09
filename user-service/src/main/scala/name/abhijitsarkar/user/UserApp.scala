package name.abhijitsarkar.user

import com.typesafe.config.ConfigFactory
import repository.MongoDBCollectionFactory.newCollection
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.stream.ActorMaterializer
import name.abhijitsarkar.user.repository.MongoDBUserRepository
import name.abhijitsarkar.user.service.UserBusinessDelegate
import name.abhijitsarkar.user.service.UserService

object UserApp extends App with UserService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  private val collection = newCollection("users")
  override val userRepository = new MongoDBUserRepository(collection) with UserBusinessDelegate

  Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))
}