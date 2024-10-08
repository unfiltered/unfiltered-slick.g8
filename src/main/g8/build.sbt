organization := "com.typesafe.slick"

name := "slick-presentation"

version := "0.1.0-SNAPSHOT"

scalaVersion := "$scala_version$"

val unusedWarnings = (
  "-Ywarn-unused" ::
  Nil
)

scalacOptions ++= unusedWarnings

Seq(Compile, Test).flatMap(c =>
  c / console / scalacOptions --= unusedWarnings
)

scalacOptions ++= "-deprecation" :: "unchecked" :: "-feature" :: Nil

val unfilteredVersion = "$unfiltered_version$"

libraryDependencies ++= List(
  "ws.unfiltered" %% "unfiltered-directives" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion,
  "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion,
  "com.typesafe.slick" %% "slick" % "$slick_version$",
  "org.slf4j" % "slf4j-nop" % "2.0.16",
  "c3p0" % "c3p0" % "0.9.1.2",
  "com.h2database" % "h2" % "2.3.232"
/*
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "org.apache.derby" % "derby" % "10.9.1.0",
  "org.hsqldb" % "hsqldb" % "2.2.8",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "mysql" % "mysql-connector-java" % "5.1.23",
  "net.sourceforge.jtds" % "jtds" % "1.2.4" % "test"
*/
)
