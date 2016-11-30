package ch4

import javax.money.convert.MonetaryConversions
import javax.money.{Monetary, MonetaryAmount}

import cats._
import cats.implicits._
import cats.kernel.Order
import ch4.Domain._

/**
  * @author Abhijit Sarkar
  */
object AnalyticsCats extends Analytics {

  implicit val moneyMonoid = new Monoid[MonetaryAmount] {
    override def empty: MonetaryAmount = Monetary.getDefaultAmountFactory
      .setCurrency("USD")
      .setNumber(0.0d)
      .create
      .asInstanceOf[MonetaryAmount]

    override def combine(x: MonetaryAmount, y: MonetaryAmount): MonetaryAmount = {
      val conversion = MonetaryConversions.getConversion(empty.getCurrency)

      y.`with`(conversion).add(x.`with`(conversion))
    }
  }

  implicit val byAmount = Order.by[MonetaryAmount, Double](_.getNumber.doubleValue)

  val conversion = MonetaryConversions.getConversion(moneyMonoid.empty.getCurrency)

  override def maxDebitOnDay(transactions: List[Transaction]): MonetaryAmount = transactions
    .filter(_.txnType == DR)
    .map(_.amount.`with`(conversion))
    .foldMap(Order[MonetaryAmount].max(_, moneyMonoid.empty))

  override def sumCRBalances(balances: List[MonetaryAmount]): MonetaryAmount = balances
    .filter(_.isPositive).foldMap(identity)
}
