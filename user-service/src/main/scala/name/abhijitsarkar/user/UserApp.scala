package name.abhijitsarkar.user

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.enhanceRouteWithConcatenation
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.stream.ActorMaterializer
import name.abhijitsarkar.user.repository.MongoDBUserRepository
import name.abhijitsarkar.user.repository.MongoDBUserRepositoryAdapter
import name.abhijitsarkar.user.service.UserBusinessDelegate
import name.abhijitsarkar.user.service.UserService
import repository.MongoDBCollectionFactory.newCollection
import scala.concurrent.ExecutionContextExecutor

object UserApp extends App with UserReadResource with UserWriteResource with ActorPlumbing {
  override implicit val system = ActorSystem("user-service")
  override implicit def executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override def config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  private val collection = newCollection("users")
  val userRepository = new MongoDBUserRepository(collection)
  val userService: UserService = new MongoDBUserRepositoryAdapter(userRepository)
  
  override val businessDelegateProps: Props = UserBusinessDelegate.props(userService, executor)
  
//  system.actorOf(businessDelegateProps)

  Http().bindAndHandle(readRoute ~ writeRoute, config.getString("http.interface"), config.getInt("http.port"))
}