# Quick Start - Using Local Kafka & Spark

## Your Setup (Local Installations)

Since you already have Kafka and Spark installed locally on Windows 11:

Kafka - Already installed locally  
Spark - Already installed locally  
PostgreSQL - Using Docker (simpler)  

---

## Simplified Execution Steps

### STEP 1: Start Local Kafka ⏱️ 1 minute

```powershell
# Start Zookeeper (if not already running)
# Navigate to your Kafka directory
cd <YOUR_KAFKA_INSTALL_DIR>

# Start Zookeeper in one terminal
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

# In a NEW terminal, start Kafka
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

**Keep both terminals open!**

---

### STEP 2: Create Kafka Topic 30 seconds

```powershell
# In a NEW terminal
cd <YOUR_KAFKA_INSTALL_DIR>

# Create topic
.\bin\windows\kafka-topics.bat --create --topic sensor-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Verify
.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
```

---

### STEP 3: Start PostgreSQL (Docker)  1 minute

```powershell
cd C:\Users\hp\IdeaProjects\spark-kafka-grafana\docker

# Start only PostgreSQL (not Kafka/Redpanda)
docker-compose up -d postgres pgadmin
```

**Verify:**
```powershell
docker ps
```

Should show `postgres-db` and `pgadmin` running.

---

### STEP 4: Build Spark Application  2 minutes

```powershell
cd C:\Users\hp\IdeaProjects\spark-kafka-grafana

sbt clean compile package
```

---

### STEP 5: Run Spark Streaming Job  Ongoing

```powershell
# Make sure SPARK_HOME is set
$env:SPARK_HOME = "<YOUR_SPARK_INSTALL_DIR>"
$env:PATH = "$env:SPARK_HOME\bin;$env:PATH"

# Run Spark job
spark-submit `
  --class StreamingJob `
  --master local[*] `
  --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1,org.postgresql:postgresql:42.7.1 `
  target\scala-2.12\spark-kafka-streaming_2.12-0.1.0-SNAPSHOT.jar
```

 **Success indicators:**
```
 Starting Spark Streaming Job...
 Connected to Kafka
 Waiting for data from Kafka topic: sensor-events
```

**Keep this terminal open!**

---

### STEP 6: Run Producer Ongoing

**Open a NEW terminal:**

```powershell
cd C:\Users\hp\IdeaProjects\spark-kafka-grafana

# Install dependency (first time only)
pip install kafka-python

# Run producer
python scripts\kafka_producer.py
```

 **Success indicators:**
```
 Starting Kafka Producer...
 Connected to Kafka
 Sent 10 messages | Latest: SensorID=5, Temp=24.32°C
```

**Keep this terminal open!**

---

### STEP 7: Verify Data Flow 1 minute

**In a NEW terminal:**

#### Check Kafka messages:
```powershell
cd <YOUR_KAFKA_INSTALL_DIR>

# Consume messages (Ctrl+C to stop)
.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic sensor-events --from-beginning
```

#### Check PostgreSQL data:
```powershell
docker exec -it postgres-db psql -U postgres -d sensors
```

```sql
-- Check data
SELECT COUNT(*) FROM sensor_metrics;
SELECT * FROM sensor_metrics ORDER BY window_start DESC LIMIT 5;

-- Exit
\q
```

---

##  Running System

After all steps, you should have:

| Terminal | Service | Status |
|----------|---------|--------|
| 1 | Zookeeper | Running |
| 2 | Kafka Server | Running |
| 3 | Spark Job | Running |
| 4 | Producer | Running |

| Docker Container | Service |
|------------------|---------|
| postgres-db | PostgreSQL |
| pgadmin | pgAdmin UI |

---

##  Access Points

- **Kafka**: localhost:9092 (local)
- **PostgreSQL**: localhost:5432 (Docker)
- **pgAdmin**: http://localhost:5050
- **Spark UI**: http://localhost:4040

---

##  Stopping Services

```powershell
# 1. Stop Producer (Ctrl+C in producer terminal)
# 2. Stop Spark (Ctrl+C in Spark terminal)
# 3. Stop Kafka (Ctrl+C in Kafka terminal)
# 4. Stop Zookeeper (Ctrl+C in Zookeeper terminal)

# 5. Stop Docker PostgreSQL
cd docker
docker-compose down
```

---

##  Restarting

Since Kafka and Spark are local, just:

1. Start Zookeeper → Kafka
2. Start Docker PostgreSQL: `docker-compose up -d postgres`
3. Run Spark job
4. Run producer

No rebuild needed unless code changes!

---

##  Optional: Skip Docker Completely

If you also have PostgreSQL installed locally:

1. Create database and table manually using `docker/postgres/init.sql`
2. Update connection in `StreamingJob.scala` if needed
3. Skip all Docker commands
4. Everything runs purely local!

---

##  Notes

- **Scripts are optional** - You can delete them or keep for reference
- **Docker-compose.yml** - Still useful for PostgreSQL, or skip if you have local PostgreSQL
- **Local Kafka** - More control, faster startup, no Docker overhead
- **Local Spark** - Better for development, easier debugging

---

##  Advantages of Your Setup

Faster startup (no Docker overhead for Kafka)  
Easier debugging (direct access to logs)  
More control over Kafka configuration  
Familiar with your existing tools  
Less memory usage (no Docker containers for Kafka)  

---

## Quick Verification

```powershell
# 1. Kafka is running
cd <YOUR_KAFKA_INSTALL_DIR>
.\bin\windows\kafka-broker-api-versions.bat --bootstrap-server localhost:9092

# 2. Topic exists
.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

# 3. PostgreSQL has data
docker exec -it postgres-db psql -U postgres -d sensors -c "SELECT COUNT(*) FROM sensor_metrics"

# Should return > 0
```

---

## Troubleshooting

**Kafka won't start:**
- Check if Zookeeper is running first
- Check if port 9092 is already in use: `netstat -an | findstr 9092`

**Topic creation fails:**
- Make sure Kafka server is running
- Check Kafka logs in `<KAFKA_DIR>\logs`

**Spark can't connect to Kafka:**
- Verify Kafka is running: `netstat -an | findstr 9092`
- Check broker is accessible: Use kafka-broker-api-versions command above

---
