package name.abhijitsarkar.user

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.HttpCharsets
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.MediaTypes.`application/json`
import name.abhijitsarkar.user.domain.User
import spray.json.DefaultJsonProtocol
import spray.json.pimpAny
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.ClientError
import akka.http.scaladsl.model.StatusCodes
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

object UserJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val requestJsonFormat = jsonFormat5(User)

  implicit def userMarshaller: ToResponseMarshaller[User] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`application/json`) { (user, charset) ⇒
      HttpResponse(entity =
        HttpEntity(ContentType(`application/json`), user.toJson.compactPrint))
    })

  case class Response(statusCode: StatusCode, message: String)

  implicit def responseMarshaller: ToResponseMarshaller[Response] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`application/json`) { (response, charset) ⇒
      HttpResponse(status = response.statusCode.intValue, entity =
        HttpEntity(ContentType(`application/json`), response.message.toJson.compactPrint))
    })
}