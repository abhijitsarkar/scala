package name.abhijitsarkar.scala.scauth.api

import akka.http.scaladsl.model.HttpRequest
import scala.concurrent.Future
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.model.ResponseEntity

trait OAuthService[A] {
  def sendWithAuthorizationQueryParams(request: OAuthRequest)(implicit unmarshaller: Unmarshaller[ResponseEntity, A]): Future[A] = {
    val httpRequest = request.toHttpRequestWithAuthorizationQueryParams

    sendAndReceive(httpRequest, request.signature)
  }

  def sendWithAuthorizationHeader(request: OAuthRequest)(implicit unmarshaller: Unmarshaller[ResponseEntity, A]): Future[A] = {
    val httpRequest = request.toHttpRequestWithAuthorizationHeader

    sendAndReceive(httpRequest, request.signature)
  }

  protected def sendAndReceive(httpRequest: HttpRequest, id: String)(implicit unmarshaller: Unmarshaller[ResponseEntity, A]): Future[A]
}
