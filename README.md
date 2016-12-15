# scala
My Scala projects

## References

[Typesafe Examples](https://github.com/typesafehub)

[Scala Exercises](https://www.scala-exercises.org/)

[herding cats](http://eed3si9n.com/herding-cats/)

[Cats Data Types](http://typelevel.org/cats/datatypes/state.html)

[Cats Type classes](http://typelevel.org/cats/typeclasses.html)

[Functors, Applicatives, And Monads In Pictures](http://adit.io/posts/2013-04-17-functors,_applicatives,_and_monads_in_pictures.html)

[Three Useful Monads](http://adit.io/posts/2013-06-10-three-useful-monads.html)

[Scala Constructor Parameters' Visibility](http://stackoverflow.com/questions/14694712/do-scala-constructor-parameters-default-to-private-val)

[Functors, Monads, Applicatives – can be so simple](https://thedet.wordpress.com/2012/04/28/functors-monads-applicatives-can-be-so-simple)

[For Comprehension](http://docs.scala-lang.org/tutorials/FAQ/yield.html)

```
var found = false

// Strict
List.range(1, 10)
  .filter(_ % 2 == 1 && !found)
  .foreach(x =>
    if (x == 5) found = true else println(x)
  )
```
Output:
1
3
7
9

```
var found = false

// Non-strict
for {
  x <- List.range(1, 10) if x % 2 == 1 && !found
} if (x == 5) found = true else println(x)

```
Output:
1
3

```
var found = false

// Non-strict
List.range(1, 10)
  .withFilter(_ % 2 == 1 && !found)
  .foreach(x =>
    if (x == 5) found = true else println(x)
  )
```
Output:
1
3

Can also be written as:
```
for (x <- List.range(1, 10); if x % 2 == 1 && !found)
    if (x == 5) found = true else println(x)
```

Or as:
```
for (x <- List.range(1, 10); if x % 2 == 1 && !found) {
    if (x == 5) found = true else println(x)
}
```

[Context Bounds](http://docs.scala-lang.org/tutorials/FAQ/context-bounds)

`def g[A : B](a: A) = h(a)`

is really

`def g[A](a: A)(implicit ev: B[A]) = h(a)`

Ordering

`def f[A : Ordering](a: A, b: A) = if (implicitly[Ordering[A]].lt(a, b)) a else b`

also written as
```
def f[A](a: A, b: A)(implicit ord: Ordering[A]) = {
    import ord._
    if (a < b) a else b
}
```


