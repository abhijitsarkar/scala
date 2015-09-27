package name.abhijitsarkar.scala.scauth.model

object OAuthSignatureMethod extends Enumeration {
  type OAuthSignatureMethod = Value
  val HMacSHA1 = Value

  implicit def signatureMethodToString(method: OAuthSignatureMethod) = {
    method match {
      case HMacSHA1 => "HMAC-SHA1"
    }
  }
}
