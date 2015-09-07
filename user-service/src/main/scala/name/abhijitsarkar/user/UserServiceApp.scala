package name.abhijitsarkar.user

import com.typesafe.config.ConfigFactory
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.mongodb.casbah.MongoClient
import akka.event.Logging

object UserServiceApp extends App with UserService {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  
  private val collection = MongoClient()("akka")("users")
  override val userRepository = new MongoDBUserRepository(collection) with UserBusinessDelegate
  
  Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))
}