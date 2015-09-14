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
import name.abhijitsarkar.user.service.UserService._
import spray.json.DefaultJsonProtocol
import spray.json.pimpAny
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.ClientError
import akka.http.scaladsl.model.StatusCodes
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

object UserJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userJsonFormat = jsonFormat5(User)

  implicit val nameJsonFormat = jsonFormat2(FindByNameRequest)

  implicit def userMarshaller: ToResponseMarshaller[User] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`application/json`) { (user, charset) =>
      HttpResponse(entity =
        HttpEntity(ContentType(`application/json`), user.toJson.compactPrint))
    })

  implicit def findByNameResponseMarshaller: ToResponseMarshaller[FindByNameResponse] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`application/json`) { (response, charset) =>
      HttpResponse(status = response.statusCode.intValue, entity =
        HttpEntity(ContentType(`application/json`), response.body.toJson.compactPrint))
    })

  implicit def userModificationResponseMarshaller: ToResponseMarshaller[UserModificationResponse] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`application/json`) { (response, charset) =>
      HttpResponse(status = response.statusCode.intValue, entity =
        HttpEntity(ContentType(`application/json`), response.body.toJson.compactPrint))
    })
}