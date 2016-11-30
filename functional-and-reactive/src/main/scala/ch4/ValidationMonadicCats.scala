package ch4

import java.time.OffsetDateTime

import cats.syntax.either._
import ch4.Domain.{Account, SavingsAccount}

/**
  * @author Abhijit Sarkar
  */
// https://github.com/typelevel/cats/blob/master/docs/src/main/tut/datatypes/either.md
object ValidationMonadicCats {
  val accountNumTooShort = "Account number must have a minimum length of 10 characters."
  val rateNonPositive = "Rate of interest must be positive."
  val openDateNotBeforeCloseDate = "Open date must be before close date."

  type E[A] = Either[String, A]

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
      Either.right(num)
    else Either.left(accountNumTooShort)
  }

  private def validateRate(rate: BigDecimal): E[BigDecimal] = {
    if (rate > BigDecimal(0))
      Either.right(rate)
    else Either.left(rateNonPositive)
  }

  private def validateOpenCloseDate(dateOfOpen: Option[OffsetDateTime],
                                    dateOfClose: Option[OffsetDateTime]): E[Option[OffsetDateTime]] = {
    val now = OffsetDateTime.now

    dateOfOpen.orElse(Some(now)).zip(dateOfClose.orElse(Some(now.plusDays(1)))) match {
      case List((open, close)) if (open.isBefore(close)) => Either.right(Some(open))
      case _ => Either.left(openDateNotBeforeCloseDate)
    }
  }
}
