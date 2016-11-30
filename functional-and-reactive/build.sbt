name := "functional-and-reactive"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.0"

scalacOptions := Seq(
  "-feature", "-unchecked", "-deprecation", "-encoding", "utf8"
)
val catsVersion = "0.8.1"
val monetaVersion = "1.1"
val specs2Version = "3.8.6"
val scalazVersion = "7.2.7"
val sextPrettyPrintingVersion = "0.2.5"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.typelevel" %% "cats" % catsVersion,
  "org.javamoney" % "moneta" % monetaVersion,
  "org.specs2" %% "specs2-core" % specs2Version % Test,
  "org.specs2" %% "specs2-scalacheck" % specs2Version % Test,
  "com.github.nikita-volkov" % "sext" % sextPrettyPrintingVersion % Test
)
