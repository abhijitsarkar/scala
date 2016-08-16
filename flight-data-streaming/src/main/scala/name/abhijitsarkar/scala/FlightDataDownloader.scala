package name.abhijitsarkar.scala

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import scala.sys.process._

/**
  * @author Abhijit Sarkar
  */
object FlightDataDownloader {
  val megabytes = 1024 * 1024

  def download(url: URL, out: File) = {
    if (!out.exists)
      out.mkdirs
    else if (out.isFile || !out.canRead || !out.canExecute)
      Console.err.println(s"${out.getAbsolutePath} is not a directory or no rx permissions.")

    val file = url.getFile
    val outfile = new File(out, file.substring(file.lastIndexOf('/') + 1))

    outfile.delete
    println("Beginning download...")

    val start = Instant.now

    val exitStatus = url #> outfile !

    val timeTaken = SECONDS.between(start, Instant.now)

    exitStatus match {
      case 0 => {
        println(s"Successfully downloaded: $url to: ${outfile.getAbsolutePath}.")
        println(s"File size: ${outfile.length() / megabytes} MB.")
        println(s"Time taken: $timeTaken seconds.")
      }
      case _ => {
        Console.err.println(s"Failed to download from: $url after: $timeTaken seconds.")
        if (outfile.exists) outfile.delete
      }
    }

    outfile
  }

  def extract(in: File) = {
    if (!in.isFile || !in.canRead)
      Console.err.println(s"${in.getAbsolutePath} not a file or no read permission.")

    val parentDir = in.getParent
    val filename = in.getName
    val extractedFilename = filename.substring(0, filename.lastIndexOf('.'))

    val fin = new FileInputStream(in)
    val bin = new BufferedInputStream(fin)
    val outfile = new File(parentDir, extractedFilename)
    val out = new FileOutputStream(outfile)
    val bzIn = new BZip2CompressorInputStream(bin)

    val buffer = new Array[Byte](4096)

    println("Beginning extraction...")

    val start = Instant.now

    Stream.continually(bzIn.read(buffer))
      .takeWhile(_ != -1)
      .foreach(out.write(buffer, 0, _))

    out.close
    bzIn.close

    in.delete

    val timeTaken = SECONDS.between(start, Instant.now)

    println(s"Successfully extracted: ${in.getAbsolutePath} to: ${outfile.getAbsolutePath}.")
    println(s"File size: ${outfile.length() / megabytes} MB.")
    println(s"Time taken: $timeTaken seconds.")

    outfile.getAbsolutePath
  }
}
