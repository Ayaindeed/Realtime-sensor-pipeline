ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "spark-kafka-streaming",
    libraryDependencies ++= Seq(
      // Spark Core and SQL
      "org.apache.spark" %% "spark-core" % "3.5.1",
      "org.apache.spark" %% "spark-sql" % "3.5.1",
      "org.apache.spark" %% "spark-streaming" % "3.5.1",
      
      // Kafka Integration
      "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1",
      
      // PostgreSQL JDBC Driver
      "org.postgresql" % "postgresql" % "42.7.1",
      
      // JSON Processing - use Spark's version (3.7.0-M11)
      "org.json4s" %% "json4s-jackson" % "3.7.0-M11",
      
      // Configuration
      "com.typesafe" % "config" % "1.4.3",
      
      // Logging
      "org.apache.logging.log4j" % "log4j-api" % "2.20.0",
      "org.apache.logging.log4j" % "log4j-core" % "2.20.0",
      
      // Testing
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    ),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.concat
      case x => MergeStrategy.first
    }
  )
