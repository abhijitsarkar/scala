package ch4

import java.time.OffsetDateTime
import javax.money.{Monetary, MonetaryAmount}

import ch4.Domain.{CR, DR, Transaction}
import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.scalacheck.Parameters

/**
  * @author Abhijit Sarkar
  */

class AnalyticsSpec extends Specification with ScalaCheck {
  val currencyGen: Gen[String] = Gen.oneOf("USD", "EUR", "INR")

  def moneyGen: Gen[MonetaryAmount] = for {
    currency <- currencyGen
    amount <- Arbitrary.arbitrary[Double] // suchThat (_ >= 0.0d)
  } yield Monetary.getDefaultAmountFactory
    .setCurrency(currency)
    .setNumber(amount)
    .create
    .asInstanceOf[MonetaryAmount]

  val today = OffsetDateTime.now

  def txnGen: Gen[Transaction] = for {
    id <- Gen.identifier
    accountNum <- Gen.alphaStr
    amount <- Arbitrary.arbitrary[Double]
    currency <- currencyGen
    money <- moneyGen
    txnType <- Gen.oneOf(CR, DR)
  } yield Transaction(id, accountNum, OffsetDateTime.now, money, txnType)

  implicit val arbitraryMoney = Arbitrary(moneyGen)
  implicit val arbitraryTxn = Arbitrary(txnGen)

  implicit val params = Parameters(minTestsOk = 20)
    .verbose

  "AnalyticsCats" should {
    import AnalyticsCats._
    "calculate positive sum of credit balance" in {
      val moneys = Gen.containerOf[List, MonetaryAmount](moneyGen) suchThat (!_.isEmpty)

      prop((m: List[MonetaryAmount]) => sumCRBalances(m).isPositiveOrZero).setGen(moneys)
    }

    "find the max debit on a day" in {
      val txns = Gen.containerOf[List, Transaction](txnGen) suchThat (!_.isEmpty)
      prop((t: List[Transaction]) => maxDebitOnDay(t).isPositiveOrZero).setGen(txns)
    }
  }

  "AnalyticsScalaz" should {
    import AnalyticsScalaz._
    "calculate positive sum of credit balance" in {
      val moneys = Gen.containerOf[List, MonetaryAmount](moneyGen) suchThat (!_.isEmpty)

      prop((m: List[MonetaryAmount]) => sumCRBalances(m).isPositiveOrZero).setGen(moneys)
    }

    "find the max debit on a day" in {
      val txns = Gen.containerOf[List, Transaction](txnGen) suchThat (!_.isEmpty)
      prop((t: List[Transaction]) => maxDebitOnDay(t).isPositiveOrZero).setGen(txns)
    }
  }
}
