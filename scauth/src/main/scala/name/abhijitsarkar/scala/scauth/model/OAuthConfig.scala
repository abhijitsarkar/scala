package name.abhijitsarkar.scala.scauth.model

import name.abhijitsarkar.scala.scauth.api.OAuthEncoder
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod.HMacSHA1
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod.OAuthSignatureMethod
import name.abhijitsarkar.scala.scauth.model.OAuthVersion.OAuthVersion
import name.abhijitsarkar.scala.scauth.model.OAuthVersion.OneOh
import name.abhijitsarkar.scala.scauth.util.NonceGenerator
import name.abhijitsarkar.scala.scauth.util.SimpleNonceGenerator
import name.abhijitsarkar.scala.scauth.util.SimpleTimestampGenerator
import name.abhijitsarkar.scala.scauth.util.SimpleUrlEncoder
import name.abhijitsarkar.scala.scauth.util.TimestampGenerator

case class OAuthConfig(oAuthVersion: OAuthVersion = OneOh, oAuthSignatureMethod: OAuthSignatureMethod = HMacSHA1,
  nonceGenerator: NonceGenerator = SimpleNonceGenerator,
  timestampGenerator: TimestampGenerator = SimpleTimestampGenerator,
  oAuthEncoder: OAuthEncoder = SimpleUrlEncoder)