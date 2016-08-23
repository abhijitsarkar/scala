package name.abhijitsarkar.scala

/**
 * @author Abhijit Sarkar
 */

import java.io.{ BufferedInputStream, File, FileInputStream, FileOutputStream }
import java.net.URL
import java.time.temporal.ChronoField._
import java.time.temporal.ChronoUnit.SECONDS
import java.time.{ Instant, LocalDateTime }

import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

import scala.sys.process._
import scala.util.{ Random, Try }

/**
 * @author Abhijit Sarkar
 */
object Downloader {
  val megabytes = 1024 * 1024

  val BASE_URL = "ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/daily/by_year/"
  val numYears = 1

  val currentYear = LocalDateTime.now().get(YEAR)
  val previousCentury = currentYear - 100
  val randomYears = new Random().shuffle(previousCentury to currentYear).toSeq.take(numYears)

  val extractYear: String => Int = s => Try(s.takeWhile(_ != '.').trim.toInt).getOrElse(-1)

  val urlSrc = Source.fromIterator(() => io.Source.fromURL(BASE_URL).getLines)

  val outFiles = Flow[String]
    .filter(_.contains("csv.gz"))
    .mapConcat(_.split("\\s").takeRight(1).toStream) // takeRight is empty safe :)
    .filter(s => randomYears.exists(_ == extractYear(s)))

  def download(url: URL, out: File) = {
    if (!out.exists) {
      out.mkdirs
    } else if (out.isFile || !out.canRead || !out.canExecute) {
      throw new IllegalArgumentException(s"${out.getAbsolutePath} is not a directory or no rx permissions.")
    }

    val file = url.getFile
    val outfile = new File(out, file.substring(file.lastIndexOf('/') + 1))

    outfile.delete
    println("Beginning download...")

    val start = Instant.now

    import scala.language.postfixOps // avoid deprecation warning
    val exitStatus = url #> outfile !

    val timeTaken = SECONDS.between(start, Instant.now)

    exitStatus match {
      case 0 => {
        println(s"Successfully downloaded: $url to: ${outfile.getAbsolutePath}.")
        println(s"File size: ${outfile.length() / megabytes} MB.")
        println(s"Time taken: $timeTaken seconds.")
      }
      case _ => {
        if (outfile.exists) outfile.delete
        throw new IllegalStateException(s"Failed to download from: $url after: $timeTaken seconds.")
      }
    }

    outfile
  }

  def extractFilename(in: File) = {
    val parentDir = in.getParent
    val filename = in.getName

    filename.substring(0, filename.lastIndexOf('.'))
  }

  def extract(in: File) = {
    if (!in.isFile || !in.canRead) {
      throw new IllegalArgumentException(s"${in.getAbsolutePath} not a file or no read permission.")
    }

    val extractedFilename = extractFilename(in)
    val outfile = new File(in.getParent, extractedFilename)

    val fin = new FileInputStream(in)
    val bin = new BufferedInputStream(fin)
    val out = new FileOutputStream(outfile)
    val gzIn = new GzipCompressorInputStream(bin)

    val buffer = new Array[Byte](4096)

    println("Beginning extraction...")

    val start = Instant.now

    Stream.continually(gzIn.read(buffer))
      .takeWhile(_ != -1)
      .foreach(out.write(buffer, 0, _))

    out.close
    gzIn.close

    in.delete

    val timeTaken = SECONDS.between(start, Instant.now)

    println(s"Successfully extracted: ${in.getAbsolutePath} to: ${outfile.getAbsolutePath}.")
    println(s"File size: ${outfile.length() / megabytes} MB.")
    println(s"Time taken: $timeTaken seconds.")

    outfile.getAbsolutePath
  }

  def downloadAndExtract(inDir: String) =
    urlSrc.via(outFiles).toMat(Sink.foreach(x => extract(download(new URL(s"${BASE_URL}$x"), new File(inDir)))))(Keep.right)
}
