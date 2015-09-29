package name.abhijitsarkar.scala.scauth.example

import name.abhijitsarkar.scala.scauth.model.OAuthCredentials
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing
import name.abhijitsarkar.scala.scauth.model.OAuthRequestConfig
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers.stringUnmarshaller
import name.abhijitsarkar.scala.scauth.service.SimpleOAuthService
import name.abhijitsarkar.scala.scauth.model.SimpleOAuthRequest

class YelpService(val oAuthCredentials: OAuthCredentials)(implicit val actorPlumbing: ActorPlumbing) {
  private val baseUri = "http://api.yelp.com"
  private val searchUri = s"${baseUri}/v2/search"
  private val resultLimit = "3"
  
  import actorPlumbing._
  private val oAuthService = new SimpleOAuthService()

  def searchForBusinessesByLocation(searchTerm: String, location: String) = {
    val queryParams = Map("term" -> searchTerm, "location" -> location, "limit" -> resultLimit)

    val oAuthRequestConfig = OAuthRequestConfig(baseUrl = searchUri, queryParams = queryParams)
    
    val request = SimpleOAuthRequest(oAuthCredentials, oAuthRequestConfig)

    oAuthService.sendWithAuthorizationQueryParams[String](request)
  }
}