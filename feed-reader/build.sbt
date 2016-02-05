lazy val commonSettings = Seq(
  organization := "name.abhijitsarkar.scala",
  version := "1.0",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "feed-reader",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" % "akka-stream-experimental_2.11" % "2.0.3",
      "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"
    )
  )
