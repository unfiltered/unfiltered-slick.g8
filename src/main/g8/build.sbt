organization := "com.typesafe.slick"

name := "slick-presentation"

version := "0.1.0-SNAPSHOT"

scalaVersion := "$scala_version$"

scalacOptions += "-deprecation"

// scala-compiler is declared as an optional dependency by Slick.
// You need to add it explicitly to your own project if you want
// to use the direct embedding (as in SimpleExample.scala here).
libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)

libraryDependencies ++= List(
  "net.databinder" %% "unfiltered-directives" % "$unfiltered_version$",
  "net.databinder" %% "unfiltered-filter" % "$unfiltered_version$",
  "net.databinder" %% "unfiltered-jetty" % "$unfiltered_version$",
  "com.typesafe.slick" %% "slick" % "$slick_version$",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "c3p0" % "c3p0" % "0.9.1.2",
  "com.h2database" % "h2" % "1.3.166",
  "org.xerial" % "sqlite-jdbc" % "3.7.2"
/*
  "org.apache.derby" % "derby" % "10.9.1.0",
  "org.hsqldb" % "hsqldb" % "2.2.8",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "mysql" % "mysql-connector-java" % "5.1.23",
  "net.sourceforge.jtds" % "jtds" % "1.2.4" % "test"
*/
)
