package name.abhijitsarkar.scala.scauth.model

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import akka.http.scaladsl.model.HttpMethods.POST
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod.HMacSHA1
import name.abhijitsarkar.scala.scauth.model.OAuthVersion.ONE_OH

class HmacSHA1SignatureSpec extends FlatSpec with Matchers {
  it should "generate the HMAC-SHA1 signature" in {
    val baseUrl = "https://api.twitter.com/1/statuses/update.json"
    val queryParams: Map[String, String] = Map("status" -> "Hello Ladies + Gentlemen, a signed OAuth request!",
      "include_entities" -> "true",
      "oauth_consumer_key" -> "xvz1evFS4wEEPTGEFPHBog",
      "oauth_nonce" -> "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg",
      "oauth_signature_method" -> HMacSHA1,
      "oauth_timestamp" -> "1318622958",
      "oauth_token" -> "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb",
      "oauth_version" -> ONE_OH)
    val consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw"
    val tokenSecret = Some("LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE")

    val sign = HmacSHA1Signature(requestMethod = POST, baseUrl = baseUrl, queryParams = queryParams,
      consumerSecret = consumerSecret,
      tokenSecret = tokenSecret).newInstance

    sign shouldBe ("tnnArxj06cWHq44gCs1OSKk/jLY=")
  }
}