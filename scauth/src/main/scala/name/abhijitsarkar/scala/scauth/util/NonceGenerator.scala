package name.abhijitsarkar.scala.scauth.util

import java.util.UUID


trait NonceGenerator {
  def generateNonce: String
}

object SimpleNonceGenerator extends NonceGenerator {
  override def generateNonce = {
    String.valueOf(UUID.randomUUID().getMostSignificantBits)
  }
}