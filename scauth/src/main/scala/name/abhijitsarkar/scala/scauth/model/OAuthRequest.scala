package name.abhijitsarkar.scala.scauth.model

import scala.collection.immutable.Seq
import OAuthSignatureMethod._
import OAuthVersion._
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import name.abhijitsarkar.scala.scauth.util.NonceGenerator
import name.abhijitsarkar.scala.scauth.util.SimpleNonceGenerator
import name.abhijitsarkar.scala.scauth.util.SimpleTimestampGenerator
import name.abhijitsarkar.scala.scauth.util.TimestampGenerator
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import scala.concurrent.Future
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.model.ResponseEntity

case class OAuthRequest[A](val consumerKey: String, val consumerSecret: String, val token: String, tokenSecret: String,
    val requestMethod: HttpMethod = GET, baseUrl: String, queryParams: Map[String, String] = Map(),
    headers: Seq[HttpHeader] = Seq.empty, entity: Either[String, Array[Byte]] = Left(""),
    oAuthVersion: OAuthVersion = ONE_OH, oAuthSignatureMethod: OAuthSignatureMethod = HMacSHA1,
    nonceGenerator: NonceGenerator = SimpleNonceGenerator,
    timestampGenerator: TimestampGenerator = SimpleTimestampGenerator)(implicit val system: ActorSystem, implicit val executionContext: ExecutionContext,
        implicit val materializer: ActorMaterializer, implicit val unmarshaller: Unmarshaller[ResponseEntity, A]) {
  
  private val params: Map[String, String] = queryParams + 
    ("oauth_consumer_key" -> consumerKey,
    "oauth_nonce" -> nonceGenerator.generateNonce,
    "oauth_signature_method" -> oAuthSignatureMethod,
    "oauth_timestamp" -> timestampGenerator.generateTimestampInSeconds,
    "oauth_token" -> token,
    "oauth_version" -> oAuthVersion)

  private val signature = oAuthSignatureMethod match {
    case HMacSHA1 => HmacSHA1Signature(requestMethod = requestMethod, baseUrl = baseUrl, queryParams = params,
      consumerSecret = consumerSecret,
      tokenSecret = Some(tokenSecret)).newInstance
  }

  private val uri = Uri(baseUrl).withQuery(params + ("oauth_signature" -> signature))

  private val entityStr = entity.fold(left => left, right => new String(right.map(_.toChar)))

  private val httpRequest = HttpRequest(uri = uri, method = requestMethod, headers = headers).withEntity(entityStr)

  def sendAndReceive: Future[A] = {
    val response = Http().singleRequest(httpRequest)

    response.flatMap { r => Unmarshal(r.entity).to[A] }
  }
}