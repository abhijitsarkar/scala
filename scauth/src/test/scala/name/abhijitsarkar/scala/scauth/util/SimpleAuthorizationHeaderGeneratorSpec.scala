package name.abhijitsarkar.scala.scauth.util

import org.scalatest.Matchers
import org.scalatest.FlatSpec

import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod._
import name.abhijitsarkar.scala.scauth.model.OAuthVersion._
import name.abhijitsarkar.scala.scauth.util.SimpleAuthorizationHeaderGenerator.generateAuthorizationHeader
import akka.http.scaladsl.model.headers

class SimpleAuthorizationHeaderGeneratorSpec extends FlatSpec with Matchers {
  private val authorizationParams: Map[String, String] =
    Map("oauth_consumer_key" -> "xvz1evFS4wEEPTGEFPHBog",
      "oauth_nonce" -> "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg",
      "oauth_signature" -> "tnnArxj06cWHq44gCs1OSKk/jLY=",
      "oauth_signature_method" -> HMacSHA1,
      "oauth_timestamp" -> "1318622958",
      "oauth_token" -> "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb",
      "oauth_version" -> OneOh)

  val expectedHeaderValue = """OAuth oauth_consumer_key="xvz1evFS4wEEPTGEFPHBog", 
        oauth_nonce="kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg", 
        oauth_signature="tnnArxj06cWHq44gCs1OSKk%2FjLY%3D", 
        oauth_signature_method="HMAC-SHA1", 
        oauth_timestamp="1318622958", 
        oauth_token="370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb",
        oauth_version="1.0""""

  // multiline header to aid readability must be cleaned up
  val cleanedExpectedHeaderValue = expectedHeaderValue.filterNot { _.isControl }.replaceAll(",\\s+", ", ")

  it should "generate header as expected" in {
    val actualHeader = generateAuthorizationHeader(authorizationParams)

    actualHeader shouldBe (headers.RawHeader("Authorization", cleanedExpectedHeaderValue))
  }
}