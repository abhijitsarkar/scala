package ch4

import java.time.OffsetDateTime

import ch4.Domain.{Account, SavingsAccount}

import scalaz.Scalaz._
import scalaz.ValidationNel

/**
  * @author Abhijit Sarkar
  */

// https://gist.github.com/josdirksen/5c9b9cd92bcd1265ea6d
// Not sure about this one, not in official repo anymore: http://scalaz.github.io/scalaz/scalaz-2.9.1-6.0.4/doc.sxr/scalaz/example/ExampleValidation.scala.html#25462
object ValidationApplicativeScalaz {
  val accountNumTooShort = "Account number must have a minimum length of 10 characters."
  val rateNonPositive = "Rate of interest must be positive."
  val openDateNotBeforeCloseDate = "Open date must be before close date."

  type V[A] = ValidationNel[String, A]

  def savingsAccount(num: String,
                     name: String,
                     rate: BigDecimal,
                     dateOfOpen: Option[OffsetDateTime],
                     dateOfClose: Option[OffsetDateTime],
                     balance: BigDecimal): V[Account] = {
    // Validation is also an Applicative Functor, if the type of the error side of the validation is a Semigroup.
    // A number of computations are tried. If all succeed, a function can combine them into a Success. If any
    // of them fails, the individual errors are accumulated.
    (validateAccountNum(num) |@| validateRate(rate) |@| validateOpenCloseDate(dateOfOpen, dateOfClose))
      .apply((n, r, d) => SavingsAccount(n, name, r, d, dateOfClose, balance))
  }

  private def validateAccountNum(num: String): V[String] = {
    if (num.size >= 10)
      num.success
    else accountNumTooShort.failure
  }.toValidationNel

  private def validateRate(rate: BigDecimal): V[BigDecimal] = {
    if (rate > BigDecimal(0))
      rate.success
    else rateNonPositive.failure
  }.toValidationNel

  private def validateOpenCloseDate(dateOfOpen: Option[OffsetDateTime],
                                    dateOfClose: Option[OffsetDateTime]): V[Option[OffsetDateTime]] = {
    val now = OffsetDateTime.now

    dateOfOpen.orElse(Some(now)).zip(dateOfClose.orElse(Some(now.plusDays(1)))) match {
      case List((open, close)) if (open.isBefore(close)) => Some(open).success
      case _ => openDateNotBeforeCloseDate.failure
    }
  }.toValidationNel
}
