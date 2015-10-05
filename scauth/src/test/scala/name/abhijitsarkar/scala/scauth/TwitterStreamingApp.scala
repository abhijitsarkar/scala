package name.abhijitsarkar.scala.scauth

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import akka.stream.SinkShape
import akka.stream.scaladsl.FlattenStrategy
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.FlowGraph
import akka.stream.scaladsl.Sink
import name.abhijitsarkar.scala.scauth.model.OAuthCredentials
import name.abhijitsarkar.scala.scauth.model.Tweet
import name.abhijitsarkar.scala.scauth.model.TwitterJsonSupport.parseTweet
import name.abhijitsarkar.scala.scauth.service.TwitterSubscriber
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing
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

  val subscriber = Sink.actorSubscriber(TwitterSubscriber.props("subscriber"))

  val allTweets = Flow[HttpResponse].map { _.entity.dataBytes }.flatten(FlattenStrategy.concat).map {
    b => parseTweet(b.utf8String)
  }

  //  val isAfterEpoch = (t: Tweet) => t.createdAt.getYear > 1970
  //
  //  val goodTweets = allTweets.filter { isAfterEpoch }.toMat(goodSubscriber)(Keep.right)

  // The negation of isAfterEpoch is a function that applies isAfterEpoch to its argument and negates the result.
  //  val badTweets = allTweets.filter { !isAfterEpoch(_) }.toMat(badSubscriber)(Keep.right)

  //  val pub = Sink.fanoutPublisher[Tweet](1, 1)

  val partial = FlowGraph.partial() { implicit builder =>
    import FlowGraph.Implicits._

    val sink = builder.add(allTweets.to(subscriber))

    SinkShape[HttpResponse](sink)
  }

  val twitterService = new TwitterStreamingService[Tweet](oAuthCredentials, partial)

  twitterService.stream(None, Some("narendramodi"))
}