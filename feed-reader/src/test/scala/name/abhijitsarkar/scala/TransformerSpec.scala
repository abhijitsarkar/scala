package name.abhijitsarkar.scala

import name.abhijitsarkar.java.{NoaaClient => JavaNoaaClient, Transformer => JavaTransformer}
import org.scalatest.{FlatSpec, Matchers}

/**
  * @author Abhijit Sarkar
  */
class TransformerSpec extends FlatSpec with Matchers {
  "Transformer" should "extract temperature" in {
    Transformer.run(NoaaClient.currentConditionsPath(), "temp_f", "*.xml") should be (true)
  }

  "Java Transformer" should "extract temperature" in {
    JavaTransformer.run(JavaNoaaClient.currentConditionsPath(false), "temp_f", "*.xml") should be (true)
  }
}
