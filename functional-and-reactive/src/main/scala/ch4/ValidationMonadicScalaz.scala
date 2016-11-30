package ch4

import java.time.OffsetDateTime

import ch4.Domain.{Account, SavingsAccount}

import scalaz.Scalaz._
import scalaz.\/

/**
  * @author Abhijit Sarkar
  */

// http://eed3si9n.com/learning-scalaz/Either.html
object ValidationMonadicScalaz {
  val accountNumTooShort = "Account number must have a minimum length of 10 characters."
  val rateNonPositive = "Rate of interest must be positive."
  val openDateNotBeforeCloseDate = "Open date must be before close date."

  // \/ is right-biased, and so is Either in Scala 2.12.0+
  type E[A] = \/[String, A]

  def savingsAccount(num: String,
                     name: String,
                     rate: BigDecimal,
                     dateOfOpen: Option[OffsetDateTime],
                     dateOfClose: Option[OffsetDateTime],
                     balance: BigDecimal): E[Account] = {
    for {
      n <- validateAccountNum(num)
      r <- validateRate(rate)
      d <- validateOpenCloseDate(dateOfOpen, dateOfClose)
    } yield SavingsAccount(n, name, r, d, dateOfClose, balance)
  }

  private def validateAccountNum(num: String): E[String] = {
    if (num.size >= 10)
      num.right[String]
    else accountNumTooShort.left[String]
  }

  private def validateRate(rate: BigDecimal): E[BigDecimal] = {
    if (rate > BigDecimal(0))
      rate.right[String]
    else rateNonPositive.left[BigDecimal]
  }

  private def validateOpenCloseDate(dateOfOpen: Option[OffsetDateTime],
                                    dateOfClose: Option[OffsetDateTime]): E[Option[OffsetDateTime]] = {
    val now = OffsetDateTime.now

    dateOfOpen.orElse(Some(now)).zip(dateOfClose.orElse(Some(now.plusDays(1)))) match {
      case List((open, close)) if (open.isBefore(close)) => Some(open).right[String]
      case _ => openDateNotBeforeCloseDate.left[Option[OffsetDateTime]]
    }
  }
}
