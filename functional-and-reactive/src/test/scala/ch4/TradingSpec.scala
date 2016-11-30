package ch4

import org.specs2.mutable.Specification

/**
  * @author Abhijit Sarkar
  */
class TradingSpec extends Specification {

  import ch4.trading.TradeModel._
  import ch4.trading.TradingInterpreter._

  "Trading" should {
    "just effing work" in {
      val clientOrders = List(
        Map("no" -> "1",
          "customer" -> "John Doe",
          "instrument" -> "ins1/3/3.0"),
        Map("no" -> " 2 ",
          "customer" -> "Johnny Appleseed",
          "instrument" -> "ins2/5/5.0")
      )
      val trades: List[Trade] = tradeGeneration(NewYork, "broker", List("John Doe, Johnny Appleseed")).run(clientOrders)

      import sext._

      println(trades.valueTreeString)

      trades mustNotEqual Nil
    }
  }
}
