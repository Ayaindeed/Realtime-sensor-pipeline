import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import scala.util.Random
import java.util.Properties
import java.time.Instant

object KafkaProducer {
  
  def main(args: Array[String]): Unit = {
    
    println("🚀 Starting Kafka Producer...")
    
    // Kafka Configuration
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    props.put(ProducerConfig.RETRIES_CONFIG, "3")
    
    val producer = new KafkaProducer[String, String](props)
    val topic = "sensor-events"
    val random = new Random()
    
    println(s"✅ Connected to Kafka at localhost:9092")
    println(s"📡 Sending events to topic: $topic")
    println("Press Ctrl+C to stop...")
    println()
    
    var messageCount = 0
    
    try {
      // Generate and send sensor events continuously
      while (true) {
        // Generate random sensor data
        val sensorId = random.nextInt(10) + 1  // Sensor IDs 1-10
        val temperature = 15.0 + random.nextDouble() * 20.0  // 15-35°C
        val humidity = 30.0 + random.nextDouble() * 60.0     // 30-90%
        val timestamp = Instant.now().getEpochSecond
        
        // Create JSON message
        val json = s"""{"sensorId":$sensorId,"temperature":${f"$temperature%.2f"},"humidity":${f"$humidity%.2f"},"timestamp":$timestamp}"""
        
        // Send to Kafka
        val record = new ProducerRecord[String, String](topic, sensorId.toString, json)
        producer.send(record)
        
        messageCount += 1
        
        if (messageCount % 10 == 0) {
          println(s"✅ Sent $messageCount messages | Latest: SensorID=$sensorId, Temp=${f"$temperature%.2f"}°C, Humidity=${f"$humidity%.2f"}%")
        }
        
        // Send one message per second
        Thread.sleep(1000)
      }
    } catch {
      case e: InterruptedException =>
        println("\n⚠️  Producer interrupted")
      case e: Exception =>
        println(s"❌ Error: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      println(s"\n📊 Total messages sent: $messageCount")
      producer.close()
      println("✅ Producer closed")
    }
  }
}
