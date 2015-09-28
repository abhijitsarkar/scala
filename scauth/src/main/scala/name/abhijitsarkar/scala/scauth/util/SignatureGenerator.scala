package name.abhijitsarkar.scala.scauth.util

import name.abhijitsarkar.scala.scauth.api.OAuthSignature
import name.abhijitsarkar.scala.scauth.model.OAuthRequestConfig
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod._
import name.abhijitsarkar.scala.scauth.model.OAuthCredentials
import name.abhijitsarkar.scala.scauth.model.HmacSHA1Signature
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod

trait SignatureGenerator {
  def generateOAuthSignature(oAuthSignatureMethod: OAuthSignatureMethod, authorizationParams: Map[String, String],
    oAuthRequestConfig: OAuthRequestConfig, oAuthCredentials: OAuthCredentials): String
}

object SimpleSignatureGenerator extends SignatureGenerator {
  override def generateOAuthSignature(oAuthSignatureMethod: OAuthSignatureMethod, authorizationParams: Map[String, String],
    oAuthRequestConfig: OAuthRequestConfig, oAuthCredentials: OAuthCredentials) = {
    val queryParams = oAuthRequestConfig.queryParams ++ authorizationParams

    oAuthSignatureMethod match {
      case HMacSHA1 => HmacSHA1Signature(oAuthRequestConfig.copy(
        queryParams = queryParams),
        consumerSecret = oAuthCredentials.consumerSecret,
        tokenSecret = oAuthCredentials.tokenSecret).newInstance
    }
  }
}