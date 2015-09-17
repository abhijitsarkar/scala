package name.abhijitsarkar.user

import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.enhanceRouteWithConcatenation
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import name.abhijitsarkar.user.controller.UserReadResource
import name.abhijitsarkar.user.controller.UserWriteResource
import name.abhijitsarkar.user.repository.MongoDBUserRepository
import name.abhijitsarkar.user.service.UserBusinessDelegate
import repository.MongoDBCollectionFactory.newCollection
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.event.Logging

object UserApp extends App with UserReadResource with UserWriteResource {
  override implicit val system = ActorSystem("user-service")
  override implicit def executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override def config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  
  private val collection = newCollection("users")
  val userRepository = MongoDBUserRepository(collection)

  override val businessDelegateProps: Props = UserBusinessDelegate.props(userRepository, executor)

  Http().bindAndHandle(readRoute ~ writeRoute, config.getString("http.interface"), config.getInt("http.port"))
}