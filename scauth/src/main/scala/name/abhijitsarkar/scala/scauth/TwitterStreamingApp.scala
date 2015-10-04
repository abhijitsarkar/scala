package name.abhijitsarkar.scala.scauth

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import name.abhijitsarkar.scala.scauth.model.OAuthCredentials
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing
import scala.concurrent.ExecutionContext
import name.abhijitsarkar.scala.scauth.service.TwitterStreamingService

object TwitterStreamingApp extends App {
  implicit val system = ActorSystem("twitter")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = { implicitly }

  require(args.size == 4, "Usage: YelpApp <consumerKey> <consumerSecret> <token> <tokenSecret>")

  private val consumerKey = args(0).trim
  private val consumerSecret = args(1).trim
  private val token = args(2).trim
  private val tokenSecret = args(3).trim

  val oAuthCredentials = OAuthCredentials(consumerKey, consumerSecret, Some(token), Some(tokenSecret))
  implicit val actorPlumbing: ActorPlumbing = ActorPlumbing()

  val twitterService = new TwitterStreamingService(oAuthCredentials)

  twitterService.stream(None, Some("narendramodi"))
}