package name.abhijitsarkar.scala

import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Abhijit Sarkar
  */
class TransformerSpec extends FlatSpec with Matchers {
  "Transformer" should "extract temperature" in {
    Transformer.run
  }
}
