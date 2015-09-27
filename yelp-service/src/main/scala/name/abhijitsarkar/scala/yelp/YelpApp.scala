package name.abhijitsarkar.scala.yelp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object YelpApp extends App {
  implicit val system = ActorSystem("yelp")
  implicit val materializer = ActorMaterializer()

  require(args.size == 4, "Usage: YelpApp <consumerKey> <consumerSecret> <token> <tokenSecret>")

  private val consumerKey = args(0)
  private val consumerSecret = args(1).trim
  private val token = args(2).trim
  private val tokenSecret = args(3).trim

  val yelpService = new YelpService(consumerKey, consumerSecret, token, tokenSecret)

  val searchResults = yelpService.searchForBusinessesByLocation("dinner", "San Francisco, CA")

  searchResults.onComplete {
    _ match {
      case Success(results) => println(results)
      case _ => println("Bad Yelp!")
    }
  }
}