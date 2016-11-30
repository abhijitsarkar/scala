package ch4

import javax.money.convert.MonetaryConversions
import javax.money.{Monetary, MonetaryAmount}

import ch4.Domain._

import scalaz.{Monoid, Order}
import scalaz.Order._
import scalaz.Scalaz._

/**
  * @author Abhijit Sarkar
  */
object AnalyticsScalaz extends Analytics {

  implicit val moneyMonoid = new Monoid[MonetaryAmount] {
    override def zero: MonetaryAmount = Monetary.getDefaultAmountFactory
      .setCurrency("USD")
      .setNumber(0.0d)
      .create
      .asInstanceOf[MonetaryAmount]

    override def append(x: MonetaryAmount, y: => MonetaryAmount): MonetaryAmount = {
      val conversion = MonetaryConversions.getConversion(zero.getCurrency)

      y.`with`(conversion).add(x.`with`(conversion))
    }
  }

  implicit val byAmount = orderBy((_: MonetaryAmount).getNumber.doubleValue)

  val conversion = MonetaryConversions.getConversion(moneyMonoid.zero.getCurrency)

  override def maxDebitOnDay(transactions: List[Transaction]): MonetaryAmount = transactions
    .filter(_.txnType == DR)
    .map(_.amount.`with`(conversion))
    .foldMap(Order[MonetaryAmount].max(mzero[MonetaryAmount], _))

  override def sumCRBalances(balances: List[MonetaryAmount]): MonetaryAmount = balances
    .filter(_.isPositive).foldMap(identity) // |+| is the append
}
