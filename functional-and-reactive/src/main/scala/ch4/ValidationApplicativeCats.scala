package ch4

import java.time.OffsetDateTime

import cats.Apply
import cats.data.ValidatedNel
import cats.implicits._
import ch4.Domain.{Account, SavingsAccount}

/**
  * @author Abhijit Sarkar
  */

// https://github.com/typelevel/cats/blob/master/docs/src/main/tut/datatypes/validated.md
object ValidationApplicativeCats {
  val accountNumTooShort = "Account number must have a minimum length of 10 characters."
  val rateNonPositive = "Rate of interest must be positive."
  val openDateNotBeforeCloseDate = "Open date must be before close date."

  // Can also be written as the anonymously type projection ({type V[A] = ValidatedNel[String, A]})#V.
  type V[A] = ValidatedNel[String, A]

  def savingsAccount(num: String,
                     name: String,
                     rate: BigDecimal,
                     dateOfOpen: Option[OffsetDateTime],
                     dateOfClose: Option[OffsetDateTime],
                     balance: BigDecimal): V[Account] = {
    Apply[V].map3(validateAccountNum(num), validateRate(rate),
      validateOpenCloseDate(dateOfOpen, dateOfClose)) {
      case (n, r, d) => SavingsAccount(n, name, r, d, dateOfClose, balance)
    }
  }

  private def validateAccountNum(num: String): V[String] = {
    if (num.size >= 10)
      num.valid[String]
    else accountNumTooShort.invalid[String]
  }.toValidatedNel

  private def validateRate(rate: BigDecimal): V[BigDecimal] = {
    if (rate > BigDecimal(0))
      rate.valid[String]
    else rateNonPositive.invalid[BigDecimal]
  }.toValidatedNel

  private def validateOpenCloseDate(dateOfOpen: Option[OffsetDateTime],
                                    dateOfClose: Option[OffsetDateTime]): V[Option[OffsetDateTime]] = {
    val now = OffsetDateTime.now

    dateOfOpen.orElse(Some(now)).zip(dateOfClose.orElse(Some(now.plusDays(1)))) match {
      case List((open, close)) if (open.isBefore(close)) => Some(open).valid[String]
      case _ => openDateNotBeforeCloseDate.invalid[Option[OffsetDateTime]]
    }
  }.toValidatedNel

}
