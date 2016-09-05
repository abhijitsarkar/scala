name := """99-scala"""

organization := "name.abhijitsarkar.scala"
version := "1.0.0-SNAPSHOT"
scalaVersion := "2.11.8"
scalacOptions := Seq(
  "-feature", "-unchecked", "-deprecation", "-encoding", "utf8"
)

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test"
)

fork in run := true
