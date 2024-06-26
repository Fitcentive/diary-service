name := "diary"

version := "1.0"

lazy val `diary` = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLogback)

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  jdbc,
  ws,
  evolutions,
  caffeine,
  // Neo4j
  "org.neo4j.driver"    % "neo4j-java-driver" % "4.4.7",
  "io.github.neotypes" %% "neotypes-core"     % "0.21.0",
  // Anorm
  "org.playframework.anorm" %% "anorm"          % "2.6.10",
  "org.playframework.anorm" %% "anorm-postgres" % "2.6.10",
  //Cats
  "org.typelevel" %% "cats-core"   % "2.7.0",
  "org.typelevel" %% "cats-effect" % "3.3.4",
  // App sdk
  "io.fitcentive" %% "app-sdk"          % "1.0.0",
  "io.fitcentive" %% "message-registry" % "1.0.0",
  // SMTP
  "javax.mail"   % "javax.mail-api" % "1.6.2",
  "com.sun.mail" % "javax.mail"     % "1.6.2",
  //Logging
  "ch.qos.logback"       % "logback-classic"          % "1.3.0-alpha10",
  "net.logstash.logback" % "logstash-logback-encoder" % "7.0.1",
  // Image support
  "org.apache.xmlgraphics" % "batik-transcoder" % "1.14",
  "org.apache.xmlgraphics" % "batik-codec"      % "1.14",
  specs2                   % Test,
  guice
)

dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core"        % "2.11.4",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.11.4",
  "com.fasterxml.jackson.core" % "jackson-databind"    % "2.11.4",
)

Universal / javaOptions ++= Seq("-Dpidfile.path=/dev/null")

Universal / javaOptions ++= Seq(
  // -J params will be added as jvm parameters
  "-J-Xmx128m",
  "-J-Xms64m"
)
