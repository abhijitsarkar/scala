package name.abhijitsarkar.scala.scauth.model

import scala.collection.immutable.Seq
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod.signatureMethodToString
import name.abhijitsarkar.scala.scauth.model.OAuthVersion.versionToString
import name.abhijitsarkar.scala.scauth.util.SimpleSignatureGenerator.generateOAuthSignature
import name.abhijitsarkar.scala.scauth.util.SimpleAuthorizationHeaderGenerator.generateAuthorizationHeader

trait OAuthRequest {
  private[scauth] def toHttpRequestWithAuthorizationQueryParams: HttpRequest
  private[scauth] def toHttpRequestWithAuthorizationHeader: HttpRequest
  
  private[scauth] val signature: String
}

case class SimpleOAuthRequest(oAuthCredentials: OAuthCredentials,
    oAuthRequestConfig: OAuthRequestConfig,
    oAuthConfig: OAuthConfig = OAuthConfig()) extends OAuthRequest {

  private var authorizationParams: Map[String, String] = Map("oauth_consumer_key" -> oAuthCredentials.consumerKey,
    "oauth_nonce" -> oAuthConfig.nonceGenerator.generateNonce,
    "oauth_signature_method" -> oAuthConfig.oAuthSignatureMethod,
    "oauth_timestamp" -> oAuthConfig.timestampGenerator.generateTimestampInSeconds,
    "oauth_version" -> oAuthConfig.oAuthVersion)

  oAuthCredentials.token.foreach { v => authorizationParams += ("oauth_token" -> v) }

  // not necessary to include the non OAuth params
  override private[scauth] val signature = generateOAuthSignature(oAuthConfig.oAuthSignatureMethod,
    authorizationParams, oAuthRequestConfig, oAuthCredentials)

  authorizationParams += ("oauth_signature" -> signature)

  private val entityStr = oAuthRequestConfig.entity.fold(identity, right => new String(right.map(_.toChar)))

  override private[scauth] def toHttpRequestWithAuthorizationQueryParams = {
    val queryParams: Map[String, String] = (oAuthRequestConfig.queryParams ++ authorizationParams)
    val uri = Uri(oAuthRequestConfig.baseUrl).withQuery(queryParams)

    HttpRequest(uri = uri, method = oAuthRequestConfig.requestMethod,
      headers = oAuthRequestConfig.headers).withEntity(entityStr)
  }

  override private[scauth] def toHttpRequestWithAuthorizationHeader = {
    val authorizationHeader = generateAuthorizationHeader(authorizationParams, oAuthConfig.oAuthEncoder)
    val uri = Uri(oAuthRequestConfig.baseUrl).withQuery(oAuthRequestConfig.queryParams)

    HttpRequest(uri = uri, method = oAuthRequestConfig.requestMethod,
      headers = oAuthRequestConfig.headers :+ authorizationHeader).withEntity(entityStr)
  }
}