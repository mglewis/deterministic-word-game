name := "deterministic-word-game"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "com.twitter" %% "finatra-http" % "20.4.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.1" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % "test"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.7.0-M2"

mainClass in (Compile, run) := Some("uk.co.mglewis.server.ServerMain")
