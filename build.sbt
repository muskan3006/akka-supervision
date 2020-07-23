name := "akka-supervision"

version := "0.1"

scalaVersion := "2.12.4"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.3"
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-slf4j
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.6.3"
// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
