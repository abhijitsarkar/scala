package name.abhijitsarkar.scala.scauth.example

import scala.collection.immutable.Seq
import name.abhijitsarkar.scala.scauth.model.OAuthRequest
import name.abhijitsarkar.scala.scauth.model.OAuthCredentials
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing
import name.abhijitsarkar.scala.scauth.model.OAuthRequestConfig
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers.stringUnmarshaller
import name.abhijitsarkar.scala.scauth.util.SimpleUrlEncoder

class TwitterService(val oAuthCredentials: OAuthCredentials)(implicit val actorPlumbing: ActorPlumbing) {
  private val baseUri = "https://api.twitter.com/1.1"
  private val searchUri = s"${baseUri}/search/tweets.json"
  private val resultLimit = "3"

  // Twitter search API - https://dev.twitter.com/rest/public/search
  // Various Twitter search parameters - https://dev.twitter.com/rest/reference/get/search/tweets
  def search(query: String, resultType: String = "mixed") = {
    // https://github.com/akka/akka/issues/18574
    val queryParams = Map("q" -> SimpleUrlEncoder.encode(query), "result_type" -> resultType, "count" -> resultLimit)

    val oAuthRequestConfig = OAuthRequestConfig(baseUrl = searchUri, queryParams = queryParams)

    // I've no idea why this is required to resolve the unmarshaller but it is.
    import actorPlumbing._

    val request = OAuthRequest[String](oAuthCredentials, oAuthRequestConfig)

    request.sendWithAuthorizationHeader
  }
}