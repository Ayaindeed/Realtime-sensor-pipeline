import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._
import models.SensorEvent
import java.util.Properties

object StreamingJob {
  
  def main(args: Array[String]): Unit = {
    
    // Create Spark Session
    val spark = SparkSession.builder()
      .appName("Kafka-Spark-PostgreSQL Streaming")
      .master("local[*]")
      .config("spark.sql.streaming.checkpointLocation", "./checkpoint")
      .getOrCreate()
    
    spark.sparkContext.setLogLevel("WARN")
    
    import spark.implicits._
    
    println("Starting Spark Streaming Job...")
    
    // Kafka Configuration (using local Kafka installation)
    val kafkaBootstrapServers = "localhost:9092"
    val kafkaTopic = "sensor-events"
    
    // PostgreSQL Configuration (using Docker PostgreSQL or local installation)
    val postgresUrl = "jdbc:postgresql://localhost:5432/sensors"
    val postgresUser = "postgres"
    val postgresPassword = "postgres"
    
    // Read from Kafka
    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("subscribe", kafkaTopic)
      .option("startingOffsets", "latest")
      .load()
    
    println("✅ Connected to Kafka")
    
    // Define schema for sensor events
    val sensorSchema = StructType(Seq(
      StructField("sensorId", IntegerType, nullable = false),
      StructField("temperature", DoubleType, nullable = false),
      StructField("humidity", DoubleType, nullable = false),
      StructField("timestamp", LongType, nullable = false)
    ))
    
    // Parse JSON from Kafka
    val sensorEventsDF = kafkaDF
      .selectExpr("CAST(value AS STRING) as json")
      .select(from_json($"json", sensorSchema).as("data"))
      .select("data.*")
      .withColumn("event_time", to_timestamp(from_unixtime($"timestamp")))
    
    println("✅ Parsing JSON events")
    
    // Windowed Aggregations (10-second windows)
    val aggregatedDF = sensorEventsDF
      .withWatermark("event_time", "30 seconds")
      .groupBy(
        window($"event_time", "10 seconds")
      )
      .agg(
        avg("temperature").as("avg_temp"),
        avg("humidity").as("avg_humidity"),
        count("*").as("event_count")
      )
      .select(
        $"window.start".as("window_start"),
        $"window.end".as("window_end"),
        $"avg_temp",
        $"avg_humidity",
        $"event_count"
      )
    
    println("✅ Computing windowed aggregations")
    
    // Write to PostgreSQL
    val query = aggregatedDF.writeStream
      .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        println(s"📊 Processing batch $batchId with ${batchDF.count()} records")
        
        batchDF.show(false)
        
        // Write to PostgreSQL
        val props = new Properties()
        props.setProperty("user", postgresUser)
        props.setProperty("password", postgresPassword)
        props.setProperty("driver", "org.postgresql.Driver")
        
        batchDF.write
          .mode("append")
          .jdbc(postgresUrl, "sensor_metrics", props)
        
        println(s"✅ Batch $batchId written to PostgreSQL")
      }
      .trigger(Trigger.ProcessingTime("5 seconds"))
      .start()
    
    println("✅ Streaming to PostgreSQL started")
    println("📡 Waiting for data from Kafka topic: " + kafkaTopic)
    println("Press Ctrl+C to stop...")
    
    query.awaitTermination()
  }
}
