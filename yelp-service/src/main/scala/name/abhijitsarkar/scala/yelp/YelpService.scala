package name.abhijitsarkar.scala.yelp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import name.abhijitsarkar.scala.scauth.model.HmacSHA1Signature
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod.HMacSHA1
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod.signatureMethodToString
import name.abhijitsarkar.scala.scauth.model.OAuthVersion.ONE_OH
import name.abhijitsarkar.scala.scauth.model.OAuthVersion.versionToString
import name.abhijitsarkar.scala.scauth.util.SimpleNonceGenerator.generateNonce
import name.abhijitsarkar.scala.scauth.util.SimpleTimestampGenerator.generateTimestampInSeconds
import akka.http.scaladsl.model.HttpMethods.GET
import name.abhijitsarkar.scala.scauth.model.OAuthRequest
import scala.concurrent.ExecutionContext

class YelpService(val consumerKey: String, val consumerSecret: String, val token: String, val tokenSecret: String)(implicit val system: ActorSystem, implicit val executionContext: ExecutionContext,
    implicit val materializer: ActorMaterializer) {
  private val baseUri = "http://api.yelp.com"
  private val searchUri = s"${baseUri}/v2/search"
  private val resultLimit = 3

  def searchForBusinessesByLocation(searchTerm: String, location: String) = {
    val queryParams = Map("term" -> searchTerm, "location" -> location, "limit" -> String.valueOf(resultLimit))

    val request = OAuthRequest[String](consumerKey = consumerKey, consumerSecret = consumerSecret, token = token, 
        tokenSecret = tokenSecret, baseUrl = searchUri, queryParams = queryParams)

    request.sendAndReceive
  }
}