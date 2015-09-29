package name.abhijitsarkar.scala.scauth.service

import scala.concurrent.Future
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.model.ResponseEntity
import name.abhijitsarkar.scala.scauth.api.OAuthRequest

trait OAuthService {
  def sendWithAuthorizationQueryParams[A](request: OAuthRequest)(implicit unmarshaller: Unmarshaller[ResponseEntity, A]): Future[A]
  def sendWithAuthorizationHeader[A](request: OAuthRequest)(implicit unmarshaller: Unmarshaller[ResponseEntity, A]): Future[A]
}
