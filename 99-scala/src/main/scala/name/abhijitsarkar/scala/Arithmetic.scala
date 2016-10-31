package name.abhijitsarkar.scala

import scala.annotation.tailrec

/**
  * @author Abhijit Sarkar
  */
object Arithmetic {

  implicit class IntImprovements(i: Int) {
    /**
      * P31: Determine whether a given integer number is prime.
      *
      * Solution: The solution is based on the Wikipedia article for Primality test.
      * https://en.wikipedia.org/wiki/Primality_test
      *
      * Given an input number `n`, if there exists any prime integer `m` from `2` to `√n` that evenly divides `n`,
      * then `n` is composite, otherwise it is prime.
      * We can skip the even numbers greater than `2` because those are divisible by `2`.
      * Now, any prime number can be written as `6k ± 1` for some integer `k`, with the exception of `2` and `3`.
      * Thus the divisors of `n` can be expressed as `6k ± 1` as well.
      * For `k = 1`, we check whether `n` is divisible by `5` or `7`. If not, we increment `k` by `1` and repeat the process
      * until `k` exceeds `√n` or `k`^2^ exceeds `n`.
      */
    def isPrime = {
      @tailrec
      def isPrime(k: Int): Boolean = {
        i match {
          case _ if (i <= 1) => false
          case _ if (i <= 3) => true
          case _ if (i % 2 == 0 || i % 3 == 0) => false
          case _ if (k * k <= i) => {
            if (i % k == 0 || i % (k + 2) == 0) false else isPrime(k + 6)
          }
          case _ => true
        }
      }
      isPrime(5)
    }

    /**
      * P32: Determine the greatest common divisor of two positive integer numbers.
      *
      * Solution: The solution is based on the Wikipedia article for Euclidean algorithm.
      * https://en.wikipedia.org/wiki/Euclidean_algorithm
      */
    @tailrec
    final def gcd(a: Int, b: Int): Int = {
      if (a == 0) b else if (b == 0) a

      val larger = math.max(a, b)
      val smaller = math.min(a, b)

      gcd(smaller, larger % smaller)
    }

    /**
      * P33: Determine whether two positive integer numbers are coprime.
      * Two numbers are coprime if their greatest common divisor equals 1.
      */
    def isCoprimeTo(a: Int) = {
      gcd(a, i) == 1
    }

    /**
      * P35: Determine the prime factors of a given positive integer.
      * Construct a flat list containing the prime factors in ascending order.
      *
      * The prime factors of a positive integer are the prime numbers that divide that integer exactly.
      */
    def primeFactors = {
      // From P31, we know that if n is composite, then there is at least one prime p ≤ √n that divides n.
      @tailrec
      def leastPrimeFactor(j: Int, k: Int): Int = {
        j match {
          case _ if (j <= 1) => 1
          case _ if (j % 2 == 0) => 2
          case _ if (j % 3 == 0) => 3
          case _ if (k * k <= j) => if (j % k == 0) k else if (j % (k + 2) == 0) k + 2 else leastPrimeFactor(j, k + 6)
          case _ => j
        }
      }
      // We use a Vector because the problem requires the factors in ascending order and appending to a List
      // is expensive.
      @tailrec
      def primeFactors(j: Int, v: Vector[Int]): Vector[Int] = {
        val k = leastPrimeFactor(j, 5)

        if (j == k) v :+ k else primeFactors(j / k, v :+ k)
      }
      primeFactors(i, Vector[Int]()).toList
    }

    import Lists.encodeModifiedFold

    /**
      * P36: Determine the prime factors of a given positive integer (2).
      * Construct a list containing the prime factors and their multiplicity.
      */
    def primeFactorMultiplicity = encodeModifiedFold(primeFactors)
  }

}
