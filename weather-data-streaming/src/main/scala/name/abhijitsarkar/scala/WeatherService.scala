package name.abhijitsarkar.scala

import java.io.File
import java.nio.file.{DirectoryStream, Files, Path, Paths}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO.fromPath
import akka.stream.scaladsl.{Flow, Keep, Merge, Sink, Source}
import akka.util.ByteString

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

/**
  * @author Abhijit Sarkar
  */
object WeatherService {

  import scala.collection.JavaConverters._

  def inFiles(inDir: String) = Files.newDirectoryStream(Paths.get(inDir), new DirectoryStream.Filter[Path]() {
    override def accept(entry: Path) = entry.toFile.getName.contains(".csv")
  }).iterator.asScala.toList

  def combine(a: List[Path]): Source[ByteString, Any] = a match {
    case Nil => Source.empty
    case i :: Nil => fromPath(i)
    case i :: j :: Nil => Source.combine(fromPath(i), fromPath(j))(Merge(_))
    case i :: j :: tail => Source.combine(fromPath(i), fromPath(j), combine(tail))(Merge(_))
  }

  import ElementType._

  def dailyWeathers(implicit ec :ExecutionContext) = Flow[ByteString]
    .mapConcat(_.utf8String.split("\\R").toStream)
    .mapAsyncUnordered(4)(x => Future(x.split(",").map(_.trim)))
    .filter(_.size >= 4)
    .mapAsyncUnordered(4) { arr =>
      val stationId = StationId(arr(0))

      val dateOfObservation = DateOfObservation(arr(1))

      val elementType = ElementType(arr(2))

      val elementValue = arr(3)

      val measurementFlag = arr.lift(4).map(MeasurementFlag(_))

      val qualityFlag = arr.lift(5).map(QualityFlag(_))

      val sourceFlag = arr.lift(6).map(SourceFlag(_))

      val timeOfObservation = arr.lift(7).map(TimeOfObservation(_))

      Future(DailyWeather(stationId, dateOfObservation, elementType, elementValue,
        measurementFlag, qualityFlag, sourceFlag, timeOfObservation))
    }

  import Downloader._

  val summary = Flow[DailyWeather]
    .filter(x => x.elementType == maxTemp && x.dateOfObservation.isDefined)
    .groupBy(numYears, _.dateOfObservation.map(_.year).get)
    .fold((0, 0.0f)) {
      (acc, w) => (w.dateOfObservation.map(_.year).getOrElse(-1), Try(acc._2.max(w.elementValue.toFloat)).getOrElse(0.0f))
    }
    .mergeSubstreams

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("weather")
    implicit val materializer = ActorMaterializer()
    // for use with Futures, Scheduler, etc.
    import system.dispatcher

    import WeatherConstants._

    val dir = inDir

    val downloadResult = downloadAndExtract(dir).run()

    import scala.concurrent.duration._
    Await.result(downloadResult, 2.minutes)

    def run(inDir: String) =
      combine(inFiles(inDir)).via(dailyWeathers).via(summary).toMat(
        Sink.foreach(x => println(s"Year: ${x._1}, Max temp: ${x._2}."))
      )(Keep.right) // Keeps the future

    val finalResult = run(dir).run()

    Await.result(finalResult, 10.minutes)

    system.terminate()

    new File(dir).delete
  }
}
