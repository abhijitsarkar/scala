package name.abhijitsarkar.scala.scauth.model

import name.abhijitsarkar.scala.scauth.util.TimestampGenerator
import name.abhijitsarkar.scala.scauth.util.SimpleTimestampGenerator
import name.abhijitsarkar.scala.scauth.model.OAuthVersion._
import name.abhijitsarkar.scala.scauth.util.SimpleNonceGenerator
import name.abhijitsarkar.scala.scauth.model.OAuthSignatureMethod._
import name.abhijitsarkar.scala.scauth.util.NonceGenerator

case class OAuthConfig(oAuthVersion: OAuthVersion = OneOh, oAuthSignatureMethod: OAuthSignatureMethod = HMacSHA1,
  nonceGenerator: NonceGenerator = SimpleNonceGenerator,
  timestampGenerator: TimestampGenerator = SimpleTimestampGenerator)