package name.abhijitsarkar.scala

import scala.annotation.tailrec

/**
  * @author Abhijit Sarkar
  */
object Lists {
  /**
    * P01: Find the last element of a list.
    *
    */
  @tailrec
  def lastTailRec[A](l: List[A]): Option[A] = {
    l match {
      case Nil => None
      case head :: Nil => Some(head)
      case head :: tail => lastTailRec(tail)
    }
  }

  /**
    * P01: Find the last element of a list.
    *
    */
  def lastFold[A](l: List[A]) = l.foldLeft(Option(null.asInstanceOf[A])) { (_, elem) => Some(elem) }


  /**
    * P02: Find the last but one element of a list.
    *
    */
  def penultimateTailRec[A](l: List[A]) = {
    @tailrec
    def internal[A](l: List[A]): Option[A] = {
      l match {
        case head :: _ :: Nil => Some(head)
        case _ :: tail => internal(tail)
        case _ => None
      }
    }

    internal(l)
  }

  /**
    * P02: Find the last but one element of a list.
    *
    */
  def penultimateFold[A](l: List[A]) = {
    val nothing = Option(null.asInstanceOf[A])
    l.foldLeft((nothing, nothing)) { (acc, elem) => (acc._2, Some(elem)) }._1
  }

  /**
    * P03: Find the Kth element of a list.
    *
    */
  def nthTailRec[A](n: Int, l: List[A]) = {
    @tailrec
    def internal[A](l: List[A], k: Int): Option[A] = {
      l match {
        case head :: _ if (k == n) => Some(head)
        case _ :: tail if (k < n) => internal(tail, k + 1)
        case _ => None
      }
    }

    internal(l, 0)
  }

  /**
    * P03: Find the Kth element of a list.
    *
    */
  def nthFold[A](n: Int, l: List[A]) = {
    l.foldLeft((0, Option(null.asInstanceOf[A]))) { (acc, elem) =>
      val k = acc._1

      k match {
        case n => (k + 1, Some(elem))
        case _ => (k + 1, None)
      }
    }._2
  }

  /**
    * P04: Find the number of elements of a list.
    *
    */
  def lengthTailRec[A](l: List[A]) = {
    @tailrec
    def internal[A](l: List[A], k: Int): Int = {
      l match {
        case Nil => k
        case _ :: tail => internal(tail, k + 1)
      }
    }

    internal(l, 0)
  }

  /**
    * P04: Find the number of elements of a list.
    *
    */
  def lengthFold[A](l: List[A]) = l.foldLeft(0) { (acc, _) => acc + 1 }

  /**
    * P05: Reverse a list.
    *
    */
  def reverseTailRec[A](l: List[A]) = {
    @tailrec
    def internal[A](l: List[A], reverse: List[A]): List[A] = {
      l match {
        case Nil => reverse
        case head :: tail => internal(tail, head :: reverse)
      }
    }

    internal(l, Nil)
  }

  /**
    * P05: Reverse a list.
    *
    */
  def reverseFold[A](l: List[A]) = l.foldLeft(Nil.asInstanceOf[List[A]]) { (acc, elem) => elem :: acc }

  /**
    * P06: Find out whether a list is a palindrome.
    *
    */
  def isPalindrome[A](l: List[A]) = l == reverseFold(l)

  /**
    * P07: Flatten a nested list structure.
    *
    */
  def flattenTailrec(l: List[Any]): List[Any] = {
    @tailrec
    def internal(l: List[Any], flattened: List[Any]): List[Any] = {
      l match {
        case Nil => flattened
        case head :: tail if (head.isInstanceOf[List[_]]) => internal(tail, flattened ++ flattenTailrec(head.asInstanceOf[List[_]]))
        case head :: tail => internal(tail, flattened :+ head)
      }
    }

    internal(l, Nil)
  }

  /**
    * P07: Flatten a nested list structure.
    *
    */
  def flattenFold(l: List[Any]): List[Any] = l.foldLeft(Nil.asInstanceOf[List[Any]]) { (acc, elem) =>
    elem match {
      case x: List[_] => acc ++ flattenFold(x)
      case x => acc :+ x
    }
  }

  /**
    * P08: Eliminate consecutive duplicates of list elements.
    *
    */
  def compressTailrec[A](l: List[A]) = {
    @tailrec
    def internal(a: A, l: List[A], compressed: List[A]): List[A] = {
      l match {
        case Nil => compressed
        case head :: tail if (head == a) => internal(head, tail, compressed)
        case head :: tail => internal(head, tail, compressed :+ head)
      }
    }

    internal(null.asInstanceOf[A], l, Nil)
  }

  /**
    * P08: Eliminate consecutive duplicates of list elements.
    *
    */
  def compressFold[A](l: List[A]) = {
    l.foldLeft((null.asInstanceOf[A], Nil.asInstanceOf[List[A]])) { (acc, elem) =>
      val temp = if (elem == acc._1) acc._2 else acc._2 :+ elem
      (elem, temp)
    }._2
  }

  /**
    * P09: Pack consecutive duplicates of list elements into sublists.
    *
    */
  def packTailrec[A](l: List[A]) = {
    def internal(a: A, l: List[A], acc: List[A], packed: List[Any]): List[Any] = {
      l match {
        case Nil => packed :+ acc
        case head :: tail if (head == a || acc == Nil) => internal(head, tail, head :: acc, packed)
        case head :: tail => internal(head, tail, List(head), packed :+ acc)
      }
    }

    internal(null.asInstanceOf[A], l, Nil, Nil)
  }

  /**
    * P09: Pack consecutive duplicates of list elements into sublists.
    *
    */
  def packFold[A](l: List[A]) = {
    val packed = l.foldLeft((null.asInstanceOf[A], Nil.asInstanceOf[List[A]], Nil.asInstanceOf[List[Any]])) { (acc, elem) =>
      if (elem == acc._1 || acc._2 == Nil) // keeps accumulating dupes
        (elem, elem :: acc._2, acc._3)
      else
        (elem, List(elem), acc._3 :+ acc._2) // adds accumulated dupes to result and starts new dupes accumulator
    }

    packed._3 :+ packed._2 // adds last list
  }

  /**
    * P10: Run-length encoding of a list.
    *
    */
  def encodeFold[A](l: List[A]) = {
    packFold(l).map { x =>
      val elem = x.asInstanceOf[List[A]]
      (elem.size, elem.head)
    }
  }

  /**
    * P11: Modified run-length encoding.
    *
    */
  def encodeModifiedFold[A](l: List[A]) = {
    packFold(l).map {
      _ match {
        case head :: Nil => head
        case head :: tail => (tail.size + 1, head)
      }
    }
  }

  /**
    * P12: Decode a run-length encoded list.
    *
    */
  def decode[A](encoded: List[Tuple2[Int, A]]) = {
    encoded.flatMap { x =>
      List.fill(x._1)(x._2)
    }
  }

  /**
    * P13: Run-length encoding of a list (direct solution).
    *
    */
  def encodeDirectFold[A](l: List[A]) = {
    val packed = l.foldLeft((null.asInstanceOf[A], 0, Nil.asInstanceOf[List[Tuple2[Int, A]]])) {
      (acc, elem) =>
        if (elem == acc._1 || acc._2 == 0) // keeps counting dupes
          (elem, acc._2 + 1, acc._3)
        else
          (elem, 1, acc._3 :+ (acc._2, acc._1)) // adds accumulated dupes to result and starts new dupes counter
    }

    packed._3 :+ (packed._2, packed._1) // adds last tuple
  }

  /**
    * P14: Duplicate the elements of a list.
    *
    */
  def duplicateFold[A](l: List[A]) = l.flatMap(List.fill(2)(_))

  /**
    * P15: Duplicate the elements of a list a given number of times.
    *
    */
  def duplicateNFold[A](n: Int, l: List[A]) = l.flatMap(List.fill(n)(_))

  /**
    * P16: Drop every Nth element from a list.
    *
    */
  def dropFold[A](n: Int, l: List[A]) = {
    l.foldLeft(1, Nil.asInstanceOf[List[A]]) { (acc, elem) =>
      (acc._1 % n) match {
        case 0 => (acc._1 + 1, acc._2)
        case _ => (acc._1 + 1, acc._2 :+ elem)
      }
    }._2
  }

  /**
    * P17: Split a list into two parts.
    *
    */
  def splitFold[A](n: Int, l: List[A]) = {
    val temp = l.foldLeft(1, Nil.asInstanceOf[List[A]], Nil.asInstanceOf[List[A]]) { (acc, elem) =>
      (acc._1 <= n) match {
        case true => (acc._1 + 1, acc._2 :+ elem, acc._3)
        case _ => (acc._1 + 1, acc._2, acc._3 :+ elem)
      }
    }

    (temp._2, temp._3)
  }

  /**
    * P18: Extract a slice from a list.
    *
    */
  def sliceFold[A](i: Int, k: Int, l: List[A]) = {
    l.foldLeft(0, Nil.asInstanceOf[List[A]]) { (acc, elem) =>
      if (acc._1 >= i && acc._1 < k)
        (acc._1 + 1, acc._2 :+ elem)
      else
        (acc._1 + 1, acc._2)
    }._2
  }

  /**
    * P18: Extract a slice from a list.
    *
    */
  def sliceDrop[A](i: Int, k: Int, l: List[A]) = l.drop(i).take(k - i)

  def reverseSlice[A](i: Int, j: Int, l: List[A]) = {
    val temp = l.foldLeft((0, Nil.asInstanceOf[List[A]], Nil.asInstanceOf[List[A]])) { (acc, elem) =>
      if (acc._1 >= i && acc._1 <= j)
        (acc._1 + 1, elem :: acc._2, acc._3)
      else if (acc._1 > j)
        (acc._1 + 1, Nil, acc._3 ++ acc._2 :+ elem)
      else
        (acc._1 + 1, acc._2, acc._3 :+ elem)
    }

    temp._3 ++ temp._2 // in case j is greater than last index
  }

  /**
    * P19: Rotate a list N places to the left.
    *
    * Solution: The algorithm is adapted from "Programming Pearls" by "Jon Bentley".
    *
    * Given `i = 3` and `l = List('a, 'b, 'c, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k)`,
    * lets call the segment between indices `0` and `3` (exclusive) `A`,
    * and the segment between indices `3` and `l.size` (exclusive) `B`.
    * The rotation, then, is basically a transformation of `AB` to `BA`.
    *
    * Below, a reversal operation on a segment is indicated by superscript `r`.
    *
    * Step 1: `A`^r^`B` produces `List('c, 'b, 'a, 'd, 'e, 'f, 'g, 'h, 'i, 'j, 'k)`
    *
    * Step 2: `A`^r^`B`^r^ produces `List('c, 'b, 'a, 'k, 'j, 'i, 'h, 'g, 'f, 'e, 'd)`
    *
    * Step 3: `(A`^r^`B`^r^`)`^r^ = `BA` produces `List('d, 'e, 'f, 'g, 'h, 'i, 'j, 'k, 'a, 'b, 'c)`
    *
    */
  def rotate[A](i: Int, l: List[A]) = {
    val length = l.size
    val effectiveRotationDistance = if (i > 0) i else length - math.abs(i)

    var temp = reverseSlice(0, effectiveRotationDistance - 1, l)
    temp = reverseSlice(effectiveRotationDistance, length - 1, temp)
    reverseSlice(0, length - 1, temp)
  }

  /**
    * P19: Rotate a list N places to the left.
    *
    */
  def rotateDrop[A](i: Int, l: List[A]) = {
    val length = l.size
    val effectiveRotationDistance = if (i > 0) i else length - math.abs(i)

    l.drop(effectiveRotationDistance) ++ l.take(effectiveRotationDistance)
  }

  /**
    * P20: Remove the Kth element from a list.
    *
    */
  def removeAtDrop[A](i: Int, l: List[A]) = (l.take(i) ++ l.takeRight(l.size - i - 1), l.drop(i).head)

  /**
    * P20: Remove the Kth element from a list.
    *
    */
  def removeAtSlice[A](i: Int, l: List[A]) = (sliceDrop(0, i, l) ++ sliceDrop(i + 1, l.size, l),
    sliceDrop(i, i + 1, l).head)
}
