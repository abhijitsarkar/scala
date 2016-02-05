package name.abhijitsarkar.scala

import java.io.File
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.{StandardCopyOption, Files}
import java.util.zip.ZipFile

import scala.io.Codec.{ISO8859, UTF8}

/**
  * @author Abhijit Sarkar
  */
object NoaaClient {
  val basedir = new File(System.getProperty("java.io.tmpdir"), "current_conditions")

  def currentConditionsPath(overwrite: Boolean = false): String = {
    if ((basedir.exists()) && !overwrite) return basedir.getAbsolutePath

    basedir.delete() && basedir.mkdirs()
    val zipFile = download
    extract(zipFile)
    zipFile.delete()

    basedir.getAbsolutePath
  }

  private def download = {
    val src = io.Source.fromURL("http://w1.weather.gov/xml/current_obs/all_xml.zip")(ISO8859)
    val dest = new File(basedir, "cc.zip")
    val os = Files.newOutputStream(dest.toPath)
    os.write(src.map(_.toByte).toArray)

    os.flush()
    os.close()
    src.close()

    dest
  }

  import collection.JavaConverters._

  private def extract(src: File) = {
    val zipFile = new ZipFile(src)
    val entries = zipFile.entries.asScala
    entries.foreach { e => e match {
      case _ if (e.isDirectory) => new File(basedir, e.getName).mkdirs()
      case _ => Files.copy(zipFile.getInputStream(e), new File(basedir, e.getName).toPath, REPLACE_EXISTING)
    }
    }

    zipFile.close()
  }
}
