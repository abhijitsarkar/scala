package name.abhijitsarkar.user

import scala.concurrent.ExecutionContextExecutor
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.server.Directive.addByNameNullaryApply
import akka.http.scaladsl.server.Directive.addDirectiveApply
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.logRequestResult
import akka.http.scaladsl.server.Directives.parameters
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Directives.segmentStringToPathMatcher
import akka.http.scaladsl.server.Directives.string2NR
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.http.scaladsl.server.directives.LoggingMagnet.forRequestResponseFromMarker
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.http.scaladsl.model.HttpResponse
import name.abhijitsarkar.user.domain.User
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.DefaultJsonProtocol

trait Service {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter

  case class Name(firstName: Option[String], lastName: Option[String])
  
  implicit val createAndUpdateequestFormat = jsonFormat6(User)
  
  val route = {
    logRequestResult("user-service") {
      pathPrefix("user") {
        get {
          parameters("firstName".?, "lastName".?).as(Name) { name =>
            val response = name match {
              case Name(Some(firstName), None) => Left(s"Hello $firstName")
              case Name(None, Some(lastName)) => Left(s"Hello $lastName")
              case Name(Some(firstName), Some(lastName)) => Left(s"Hello $firstName $lastName")
              case _ => Right(BadRequest -> "One of first and last names is required.")
            }
            
            complete(response)
          }
        } ~ 
        (put & entity(as[User])) { user =>
          complete(s"Hello ${user.firstName} ${user.lastName}")
        } ~ 
        (post & entity(as[User])) { user =>
          complete(s"Hello ${user.firstName} ${user.lastName}")
        } ~ 
        (delete & path(Segment)) { userId =>
          complete(s"Hello $userId")
        }
      }
    }
  }
}

object UserService extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(route, config.getString("http.interface"), config.getInt("http.port"))
}