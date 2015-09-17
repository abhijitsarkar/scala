package name.abhijitsarkar.user.controller

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.StatusCode.int2StatusCode
import name.abhijitsarkar.user.domain.User
import name.abhijitsarkar.user.repository.UserRepository.FindByIdRequest
import name.abhijitsarkar.user.repository.UserRepository.FindByIdResponse
import name.abhijitsarkar.user.repository.UserRepository.FindByNameRequest
import name.abhijitsarkar.user.repository.UserRepository.FindByNameResponse
import name.abhijitsarkar.user.repository.UserRepository.UserCreateRequest
import name.abhijitsarkar.user.repository.UserRepository.UserDeleteRequest
import name.abhijitsarkar.user.repository.UserRepository.UserModificationResponse
import name.abhijitsarkar.user.repository.UserRepository.UserUpdateRequest
import spray.json.DefaultJsonProtocol
import spray.json.pimpAny

object UserJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val userJsonFormat = jsonFormat5(User)

  implicit val findByNameRequestJsonFormat = jsonFormat2(FindByNameRequest)
  implicit val findByIdRequestJsonFormat = jsonFormat1(FindByIdRequest)
  implicit val userUpdateRequestJsonFormat = jsonFormat1(UserUpdateRequest)
  implicit val userCreateRequestJsonFormat = jsonFormat1(UserCreateRequest)
  implicit val userDeleteRequestJsonFormat = jsonFormat1(UserDeleteRequest)

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

  implicit def findByIdResponseMarshaller: ToResponseMarshaller[FindByIdResponse] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`application/json`) { (response, charset) =>
      HttpResponse(status = response.statusCode.intValue, entity =
        HttpEntity(ContentType(`application/json`), response.body.toJson.compactPrint))
    })

  implicit def userModificationResponseMarshaller: ToResponseMarshaller[UserModificationResponse] = Marshaller.oneOf(
    Marshaller.withOpenCharset(MediaTypes.`application/json`) { (response, charset) =>
      HttpResponse(status = response.statusCode.intValue, entity =
        HttpEntity(ContentType(`application/json`), response.body.getOrElse("").toJson.compactPrint))
    })
}