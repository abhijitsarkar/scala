package ch4

import javax.money.MonetaryAmount

import ch4.Domain.Transaction

/**
  * @author Abhijit Sarkar
  */
trait Analytics {
  /**
    * Given a list of transactions, identify the highest-value debit transaction that occurred during the day.
    */
  def maxDebitOnDay(transactions: List[Transaction]): MonetaryAmount

  /**
    * Given a list of client balances, compute the sum of all credit balances
    * (a credit balance means a positive balance).
    */
  def sumCRBalances(balances: List[MonetaryAmount]): MonetaryAmount
}



