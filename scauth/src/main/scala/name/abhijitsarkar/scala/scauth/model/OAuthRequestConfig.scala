package name.abhijitsarkar.scala.scauth.model

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.model.ResponseEntity
import scala.collection.immutable.Seq

case class OAuthRequestConfig(requestMethod: HttpMethod = GET, baseUrl: String,
  queryParams: Map[String, String] = Map(),
  headers: Seq[HttpHeader] = Seq.empty, entity: Either[String, Array[Byte]] = Left(""))