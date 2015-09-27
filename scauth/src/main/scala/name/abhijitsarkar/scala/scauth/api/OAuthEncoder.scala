package name.abhijitsarkar.scala.scauth.api

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8

trait OAuthEncoder {
  val encodingOverrides = Map[String, String]()
  def encode(plainText: String, charset: Charset = UTF_8): String

  def postProcess(encoded: String) = {
    encodingOverrides.foldLeft(encoded) {
      case (acc, (key, value)) => acc.replaceAll(key, value)
    }
  }
}
