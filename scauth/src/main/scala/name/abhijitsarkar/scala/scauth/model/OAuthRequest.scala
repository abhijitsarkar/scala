package name.abhijitsarkar.scala.scauth.model

import scala.collection.immutable.Seq
import scala.concurrent.Future
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.Unmarshaller
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod.signatureMethodToString
import name.abhijitsarkar.scala.scauth.model.OAuthVersion.versionToString
import name.abhijitsarkar.scala.scauth.util.ActorPlumbing
import name.abhijitsarkar.scala.scauth.util.SimpleSignatureGenerator.generateOAuthSignature
import name.abhijitsarkar.scala.scauth.util.SimpleAuthorizationHeaderGenerator.generateAuthorizationHeader
import org.slf4j.LoggerFactory

case class OAuthRequest[A](oAuthCredentials: OAuthCredentials,
    oAuthRequestConfig: OAuthRequestConfig,
    oAuthConfig: OAuthConfig = OAuthConfig())(implicit val actorPlumbing: ActorPlumbing, implicit val unmarshaller: Unmarshaller[ResponseEntity, A]) {
  private val log = LoggerFactory.getLogger(getClass())

  private var authorizationParams: Map[String, String] = Map("oauth_consumer_key" -> oAuthCredentials.consumerKey,
    "oauth_nonce" -> oAuthConfig.nonceGenerator.generateNonce,
    "oauth_signature_method" -> oAuthConfig.oAuthSignatureMethod,
    "oauth_timestamp" -> oAuthConfig.timestampGenerator.generateTimestampInSeconds,
    "oauth_version" -> oAuthConfig.oAuthVersion)

  oAuthCredentials.token.foreach { v => authorizationParams += ("oauth_token" -> v) }

  // not necessary to include the non OAuth params
  private val signature = generateOAuthSignature(oAuthConfig.oAuthSignatureMethod,
    authorizationParams, oAuthRequestConfig, oAuthCredentials)

  authorizationParams += ("oauth_signature" -> signature)

  private val entityStr = oAuthRequestConfig.entity.fold(identity, right => new String(right.map(_.toChar)))

  import actorPlumbing._

  def sendWithAuthorizationQueryParams: Future[A] = {
    val queryParams: Map[String, String] = (oAuthRequestConfig.queryParams ++ authorizationParams)

    val uri = Uri(oAuthRequestConfig.baseUrl).withQuery(queryParams)

    sendAndReceive(uri, oAuthRequestConfig.headers)
  }

  def sendWithAuthorizationHeader: Future[A] = {
    val authorizationHeader = generateAuthorizationHeader(authorizationParams, oAuthConfig.oAuthEncoder)

    val uri = Uri(oAuthRequestConfig.baseUrl).withQuery(oAuthRequestConfig.queryParams)

    sendAndReceive(uri, oAuthRequestConfig.headers :+ authorizationHeader)
  }

  private def sendAndReceive(uri: Uri, headers: Seq[HttpHeader]) = {
    val httpRequest = HttpRequest(uri = uri, method = oAuthRequestConfig.requestMethod,
      headers = headers).withEntity(entityStr)

    log.debug("Http request: {}.", httpRequest)

    val response = Http().singleRequest(httpRequest)

    response.flatMap { r => Unmarshal(r.entity).to[A] }
  }
}