package ch4

import java.time.OffsetDateTime

import org.specs2.mutable.Specification

import scalaz.IList

/**
  * @author Abhijit Sarkar
  */
class ValidationSpec extends Specification {
  "ValidationApplicativeScalaz" should {
    import ValidationApplicativeScalaz._
    "fail account number validation" in {
      val maybeAccount = savingsAccount("too short", "test", BigDecimal(1.0), None, None, BigDecimal(1.0))

      maybeAccount.fold(_.head, _ => "") mustEqual (accountNumTooShort)
    }

    "fail all validations" in {
      val maybeAccount = savingsAccount("too short", "test", BigDecimal(0.0),
        None, Some(OffsetDateTime.now.minusDays(1)), BigDecimal(1.0))

      maybeAccount.fold[IList[String]](_.list, _ => IList.empty).toList must
        containAllOf(Seq(accountNumTooShort, rateNonPositive, openDateNotBeforeCloseDate))
    }

    "create new savings account" in {
      val accNum = "a" * 10
      val maybeAccount = savingsAccount(accNum, "test", BigDecimal(1.0),
        None, None, BigDecimal(1.0))

      maybeAccount.toOption mustNotEqual None
      maybeAccount.toOption.get.num mustEqual accNum
    }
  }

  "ValidationApplicativeCats" should {
    import ValidationApplicativeCats._
    "fail account number validation" in {
      val maybeAccount = savingsAccount("too short", "test", BigDecimal(1.0), None, None, BigDecimal(1.0))

      maybeAccount.fold(_.head, _ => "") mustEqual (accountNumTooShort)
    }

    "fail all validations" in {
      val maybeAccount = savingsAccount("too short", "test", BigDecimal(0.0),
        None, Some(OffsetDateTime.now.minusDays(1)), BigDecimal(1.0))

      maybeAccount.fold[List[String]](_.toList, _ => Nil) must
        containAllOf(Seq(accountNumTooShort, rateNonPositive, openDateNotBeforeCloseDate))
    }

    "create new savings account" in {
      val accNum = "a" * 10
      val maybeAccount = savingsAccount(accNum, "test", BigDecimal(1.0),
        None, None, BigDecimal(1.0))

      maybeAccount.toOption mustNotEqual None
      maybeAccount.toOption.get.num mustEqual accNum
    }
  }

  "ValidationMonadicScalaz" should {
    import ValidationMonadicScalaz._

    "fail fast when there are multiple errors" in {
      val maybeAccount = savingsAccount("too short", "test", BigDecimal(0.0),
        None, Some(OffsetDateTime.now.minusDays(1)), BigDecimal(1.0))

      maybeAccount.isLeft mustEqual true
      maybeAccount.swap.getOrElse("unexpected") mustEqual accountNumTooShort
    }

    "create new savings account" in {
      val accNum = "a" * 10
      val maybeAccount = savingsAccount(accNum, "test", BigDecimal(1.0),
        None, None, BigDecimal(1.0))

      maybeAccount.isRight mustEqual true
      maybeAccount.toOption.get.num mustEqual accNum
    }
  }

  "ValidationMonadicCats" should {
    import ValidationMonadicCats._

    "fail fast when there are multiple errors" in {
      val maybeAccount = savingsAccount("too short", "test", BigDecimal(0.0),
        None, Some(OffsetDateTime.now.minusDays(1)), BigDecimal(1.0))

      maybeAccount.isLeft mustEqual true
      maybeAccount.swap.getOrElse("unexpected") mustEqual accountNumTooShort
    }

    "create new savings account" in {
      val accNum = "a" * 10
      val maybeAccount = savingsAccount(accNum, "test", BigDecimal(1.0),
        None, None, BigDecimal(1.0))

      maybeAccount.isRight mustEqual true
      maybeAccount.toOption.get.num mustEqual accNum
    }
  }
}
