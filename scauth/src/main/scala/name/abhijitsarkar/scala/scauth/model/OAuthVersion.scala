package name.abhijitsarkar.scala.scauth.model

object OAuthVersion extends Enumeration {
  type OAuthVersion = Value
  val OneOh = Value

  implicit def versionToString(version: OAuthVersion) = {
    version match {
      case OneOh => "1.0"
    }
  }
}
