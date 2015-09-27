package name.abhijitsarkar.scala.scauth.model

import OAuthSignatureMethod.HMacSHA1
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.Http
import scala.concurrent.Future
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.model.ResponseEntity
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing

case class OAuthRequest[A](oAuthCredentials: OAuthCredentials,
    oAuthRequestConfig: OAuthRequestConfig,
    oAuthConfig: OAuthConfig = OAuthConfig())
    (implicit val actorPlumbing: ActorPlumbing, implicit val unmarshaller: Unmarshaller[ResponseEntity, A]) {

  private val params: Map[String, String] = oAuthRequestConfig.queryParams +
    ("oauth_consumer_key" -> oAuthCredentials.consumerKey,
      "oauth_nonce" -> oAuthConfig.nonceGenerator.generateNonce,
      "oauth_signature_method" -> oAuthConfig.oAuthSignatureMethod,
      "oauth_timestamp" -> oAuthConfig.timestampGenerator.generateTimestampInSeconds,
      "oauth_token" -> oAuthCredentials.token,
      "oauth_version" -> oAuthConfig.oAuthVersion)

  private val signature = oAuthConfig.oAuthSignatureMethod match {
    case HMacSHA1 => HmacSHA1Signature(oAuthRequestConfig.copy(queryParams = params),
      consumerSecret = oAuthCredentials.consumerSecret,
      tokenSecret = Some(oAuthCredentials.tokenSecret)).newInstance
  }

  private val uri = Uri(oAuthRequestConfig.baseUrl).withQuery(params + ("oauth_signature" -> signature))

  private val entityStr = oAuthRequestConfig.entity.fold(identity, right => new String(right.map(_.toChar)))

  private val httpRequest = HttpRequest(uri = uri, method = oAuthRequestConfig.requestMethod,
    headers = oAuthRequestConfig.headers).withEntity(entityStr)

  import actorPlumbing._
  def sendAndReceive: Future[A] = {
    val response = Http().singleRequest(httpRequest)

    response.flatMap { r => Unmarshal(r.entity).to[A] }
  }
}