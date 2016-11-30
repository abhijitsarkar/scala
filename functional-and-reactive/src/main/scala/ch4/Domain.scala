package ch4

import java.time.OffsetDateTime
import javax.money.MonetaryAmount

/**
  * @author Abhijit Sarkar
  */
object Domain {

  sealed trait TransactionType

  case object DR extends TransactionType

  case object CR extends TransactionType

  case class Transaction(id: String, accountNum: String, date: OffsetDateTime,
                         amount: MonetaryAmount, txnType: TransactionType)

  trait Account {
    def num: String

    def name: String

    def rate: BigDecimal

    def dateOfOpen: Option[OffsetDateTime]

    def dateOfClose: Option[OffsetDateTime]

    def balance: BigDecimal
  }

  // The constructor should be private but we want to invoke it from both Scalaz and Cats validators
  final case class SavingsAccount(
                                   num: String,
                                   name: String,
                                   rate: BigDecimal,
                                   dateOfOpen: Option[OffsetDateTime],
                                   dateOfClose: Option[OffsetDateTime],
                                   balance: BigDecimal
                                 ) extends Account

}
