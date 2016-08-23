package name.abhijitsarkar.scala

import akka.stream.scaladsl.{ GraphDSL, RunnableGraph, Sink, Source, ZipWith }
import akka.stream.{ ClosedShape, UniformFanInShape }

object PartialGraph {
  private val pickMaxOfThree = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val zip1 = b.add(ZipWith[Int, Int, Int](math.max _))
    val zip2 = b.add(ZipWith[Int, Int, Int](math.max _))
    zip1.out ~> zip2.in0

    UniformFanInShape(zip2.out, zip1.in0, zip1.in1, zip2.in1)
  }

  private val resultSink = Sink.head[Int]

  /*
  * Using builder.add will make a copy of the blueprint that is passed to it
  * and return the inlets and outlets of the resulting copy so that they can be wired up.
  * Another alternative is to pass existing graphs—of any shape—into the factory method
  * that produces a new graph.
  * The difference between these approaches is that importing using builder.add
  * ignores the materialized value of the imported graph while
  * importing via the factory method allows its inclusion.
  *
  * g.run returns the materialized value of the graph, thus the return type changes according to the
  * approach used.
   */
  val g = RunnableGraph.fromGraph(GraphDSL.create(resultSink) { implicit b =>
    sink =>
      import GraphDSL.Implicits._

      // importing the partial graph will return its shape (inlets & outlets)
      val pm3 = b.add(pickMaxOfThree)

      Source.single(1) ~> pm3.in(0)
      Source.single(2) ~> pm3.in(1)
      Source.single(3) ~> pm3.in(2)

      pm3.out ~> sink.in

      ClosedShape
  })
}
