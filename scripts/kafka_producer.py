"""
Simple Kafka Producer for Sensor Events
Generates random sensor data and sends to Kafka topic
"""

import json
import time
import random
from datetime import datetime
from kafka import KafkaProducer

def create_sensor_event():
    """Generate a random sensor event"""
    sensor_id = random.randint(1, 10)
    temperature = round(15.0 + random.random() * 20.0, 2)  # 15-35°C
    humidity = round(30.0 + random.random() * 60.0, 2)     # 30-90%
    timestamp = int(time.time())
    
    return {
        "sensorId": sensor_id,
        "temperature": temperature,
        "humidity": humidity,
        "timestamp": timestamp
    }

def main():
    # Kafka Configuration
    bootstrap_servers = 'localhost:9092'
    topic = 'sensor-events'
    
    print("🚀 Starting Kafka Producer...")
    print(f"📡 Connecting to Kafka at {bootstrap_servers}")
    
    try:
        # Create Kafka Producer
        producer = KafkaProducer(
            bootstrap_servers=bootstrap_servers,
            value_serializer=lambda v: json.dumps(v).encode('utf-8'),
            key_serializer=lambda k: str(k).encode('utf-8'),
            acks='all',
            retries=3
        )
        
        print(f"✅ Connected to Kafka")
        print(f"📡 Sending events to topic: {topic}")
        print("Press Ctrl+C to stop...\n")
        
        message_count = 0
        
        while True:
            # Generate sensor event
            event = create_sensor_event()
            
            # Send to Kafka
            future = producer.send(
                topic,
                key=event['sensorId'],
                value=event
            )
            
            # Wait for confirmation
            future.get(timeout=10)
            
            message_count += 1
            
            if message_count % 10 == 0:
                print(f"✅ Sent {message_count} messages | Latest: "
                      f"SensorID={event['sensorId']}, "
                      f"Temp={event['temperature']}°C, "
                      f"Humidity={event['humidity']}%")
            
            # Send one message per second
            time.sleep(1)
            
    except KeyboardInterrupt:
        print("\n⚠️  Producer interrupted by user")
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        raise
    finally:
        print(f"\n📊 Total messages sent: {message_count}")
        producer.close()
        print("✅ Producer closed")

if __name__ == "__main__":
    main()
