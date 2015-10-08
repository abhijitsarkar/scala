package name.abhijitsarkar.scala.scauth

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.SinkShape
import akka.stream.UniformFanOutShape
import akka.stream.scaladsl.Broadcast
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.FlowGraph
import akka.stream.scaladsl.FlowGraph.Implicits.fanOut2flow
import akka.stream.scaladsl.FlowGraph.Implicits.flow2flow
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import name.abhijitsarkar.scala.scauth.model.OAuthCredentials
import name.abhijitsarkar.scala.scauth.model.Tweet
import name.abhijitsarkar.scala.scauth.model.TwitterJsonSupport.parseTweet
import name.abhijitsarkar.scala.scauth.service.TwitterStreamingService
import name.abhijitsarkar.scala.scauth.service.TwitterSubscriber
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing

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

  val beforeEpochSubscriber = Sink.actorSubscriber(TwitterSubscriber.props("BeforeEpoch"))
  val afterEpochSubscriber = Sink.actorSubscriber(TwitterSubscriber.props("AfterEpoch"))

  val tweetsFlow = Flow[ByteString].map {
    b => parseTweet(b.utf8String)
  }

  val isAfterEpoch = (t: Tweet) => t.createdAt.getYear > 1970

  val beforeEpochTweets = Flow[Tweet].filter { isAfterEpoch }

  // The negation of isAfterEpoch is a function that applies isAfterEpoch to its argument and negates the result.
  val afterEpochTweets = Flow[Tweet].filter { !isAfterEpoch(_) }

  // Good read: https://github.com/akka/akka/issues/18505
  // val pub = Sink.fanoutPublisher[Tweet](1, 1)

  val partial = FlowGraph.partial() { implicit builder =>
    import FlowGraph.Implicits._

    val broadcast = builder.add(Broadcast[Tweet](2))

    val tweets = builder.add(tweetsFlow)

    broadcast ~> beforeEpochTweets ~> beforeEpochSubscriber
    broadcast ~> afterEpochTweets ~> afterEpochSubscriber

    tweets ~> broadcast

    SinkShape(tweets.inlet)
  }

  val twitterService = new TwitterStreamingService[Tweet](oAuthCredentials, partial)

  twitterService.stream(None, Some("narendramodi"))
}