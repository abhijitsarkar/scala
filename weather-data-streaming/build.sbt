import scalariform.formatter.preferences._

name := """weather-data-streaming"""

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-stream_2.11" % "2.4.9",
  "org.apache.commons" % "commons-compress" % "1.12",
  "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.9" % "test",
  "com.typesafe.akka" % "akka-stream-testkit_2.11" % "2.4.9" % "test"
)

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)

fork in run := true
