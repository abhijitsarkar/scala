package name.abhijitsarkar.scala.scauth.model

object OAuthVersion extends Enumeration {
  type OAuthVersion = Value
  val ONE_OH = Value

  implicit def versionToString(version: OAuthVersion) = {
    version match {
      case ONE_OH => "1.0"
    }
  }
}
