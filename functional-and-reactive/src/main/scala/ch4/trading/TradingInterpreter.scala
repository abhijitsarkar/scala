package ch4.trading

import ch4.trading.TradeModel._

import scalaz.Kleisli._
import scalaz.{Order => OrderZ, _}

trait TradingInterpreter extends Trading[Account, Trade, ClientOrder, Order, Execution, Market] {

  def clientOrders: Kleisli[List, List[ClientOrder], Order] = kleisli(fromClientOrders)

  def execute(market: Market, brokerAccount: Account) = kleisli[List, Order, Execution] { order =>
    order.items.map { item =>
      Execution(brokerAccount, item.ins, "e-123", market, item.price, item.qty)
    }
  }

  def allocate(accounts: List[Account]) = kleisli[List, Execution, Trade] { execution =>
    val q = execution.quantity / accounts.size
    accounts.map { account =>
      makeTrade(account, execution.instrument, "t-123", execution.market, execution.unitPrice, q)
    }
  }
}

object TradingInterpreter extends TradingInterpreter

