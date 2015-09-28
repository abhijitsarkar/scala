package name.abhijitsarkar.scala.scauth.util

import scala.collection.immutable.ListMap

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers
import name.abhijitsarkar.scala.scauth.api.OAuthEncoder

trait AuthorizationHeaderGenerator {
  def generateAuthorizationHeader(authorizationParams: Map[String, String], oAuthEncoder: OAuthEncoder = SimpleUrlEncoder): HttpHeader
}

object SimpleAuthorizationHeaderGenerator extends AuthorizationHeaderGenerator {
  override def generateAuthorizationHeader(authorizationParams: Map[String, String],
    oAuthEncoder: OAuthEncoder = SimpleUrlEncoder) = {
    val headerStr = ListMap(authorizationParams.toSeq.sortBy(_._1): _*).view.zipWithIndex.foldLeft("OAuth ") {
      case (acc, ((key, value), index)) =>
        s"""${acc}${oAuthEncoder.encode(key)}="${oAuthEncoder.encode(value)}"${commaSpaceOrEmpty(index, authorizationParams.size)}"""
    }

    headers.RawHeader("Authorization", headerStr)
  }

  private def commaSpaceOrEmpty(index: Int, size: Int) = {
    if (index != (size - 1)) ", " else ""
  }
}