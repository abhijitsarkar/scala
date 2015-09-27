package name.abhijitsarkar.scala.scauth.example

import name.abhijitsarkar.scala.scauth.model.OAuthRequest
import name.abhijitsarkar.scala.scauth.model.OAuthCredentials
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing
import name.abhijitsarkar.scala.scauth.model.OAuthRequestConfig
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers.stringUnmarshaller

class YelpService(val oAuthCredentials: OAuthCredentials)(implicit val actorPlumbing: ActorPlumbing) {
  private val baseUri = "http://api.yelp.com"
  private val searchUri = s"${baseUri}/v2/search"
  private val resultLimit = 3

  def searchForBusinessesByLocation(searchTerm: String, location: String) = {
    val queryParams = Map("term" -> searchTerm, "location" -> location, "limit" -> String.valueOf(resultLimit))

    val oAuthRequestConfig = OAuthRequestConfig(baseUrl = searchUri, queryParams = queryParams)
    
    // I've no idea why this is required to resolve the unmarshaller but it is.
    import actorPlumbing._
    
    val request = OAuthRequest[String](oAuthCredentials, oAuthRequestConfig)

    request.sendAndReceive
  }
}