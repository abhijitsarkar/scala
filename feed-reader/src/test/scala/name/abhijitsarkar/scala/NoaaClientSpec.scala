package name.abhijitsarkar.scala

import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Abhijit Sarkar
  */
class NoaaClientSpec extends FlatSpec with Matchers {
  "NoaaClient" should "download and extract current condition files" in {
    println(NoaaClient.currentConditionsUrl())
  }
}
