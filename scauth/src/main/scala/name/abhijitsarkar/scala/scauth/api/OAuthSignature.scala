package name.abhijitsarkar.scala.scauth.api

import name.abhijitsarkar.scala.scauth.util.StringUtil.isNullOrEmpty
import scala.collection.immutable.ListMap
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.HttpMethod

trait OAuthSignature {
  def newInstance: String

  def signingKey: String

  val baseString: String
}
/**
 * @param baseUrl URL to which the request is directed, minus any query string or hash parameters.
 *   It is important to use the correct protocol here.
 * @param consumerSecret The value which identifies your application.
 * @param tokenSecret The value which identifies the account your application is acting on behalf of;
 *   may not be known yet if obtaining a request token.
 */
abstract class OAuth10Signature(requestMethod: HttpMethod, baseUrl: String, queryParams: Map[String, String],
    consumerSecret: String, tokenSecret: Option[String], oAuthEncoder: OAuthEncoder) extends OAuthSignature {
  require(!isNullOrEmpty(baseUrl), "The base URL must not be null or empty.")
  require(!isNullOrEmpty(consumerSecret), "The consumer secret must not be null or empty.")

  override val baseString = {
    // view allows for traversal only when necessary, doesn't create intermediate collection
    val queryUrl = ListMap(queryParams.toSeq.sortBy(_._1): _*).view.zipWithIndex.foldLeft("") {
      case (acc, ((key, value), index)) =>
        s"${acc}${oAuthEncoder.encode(key)}=${oAuthEncoder.encode(value)}${ampersandOrEmpty(index)}"
    }

    s"${requestMethod.name.toUpperCase}&${oAuthEncoder.encode(baseUrl)}&${oAuthEncoder.encode(queryUrl)}"
  }

  private def ampersandOrEmpty(index: Int) = {
    if (index != (queryParams.size - 1)) "&" else ""
  }

  override def signingKey: String = {
    val encodedConsumerSecret = oAuthEncoder.encode(consumerSecret)
    val encodedTokenSecret = oAuthEncoder.encode(tokenSecret.getOrElse(""))

    s"${encodedConsumerSecret}&${encodedTokenSecret}"
  }
}