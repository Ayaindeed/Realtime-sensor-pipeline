-- Grafana SQL Queries for PostgreSQL Dashboard

-- Query 1: Latest Metrics (Real-time Overview)
-- Shows the most recent windowed aggregations
SELECT 
    window_start,
    window_end,
    ROUND(avg_temp::numeric, 2) as avg_temperature,
    ROUND(avg_humidity::numeric, 2) as avg_humidity,
    event_count
FROM sensor_metrics
ORDER BY window_start DESC
LIMIT 50;

-- Query 2: Temperature Over Time (Time Series)
-- For line chart visualization
SELECT 
    window_start as time,
    avg_temp as value
FROM sensor_metrics
WHERE window_start >= NOW() - INTERVAL '1 hour'
ORDER BY window_start ASC;

-- Query 3: Humidity Over Time (Time Series)
-- For line chart visualization
SELECT 
    window_start as time,
    avg_humidity as value
FROM sensor_metrics
WHERE window_start >= NOW() - INTERVAL '1 hour'
ORDER BY window_start ASC;

-- Query 4: Event Count Over Time (Time Series)
-- Shows throughput/activity
SELECT 
    window_start as time,
    event_count as value
FROM sensor_metrics
WHERE window_start >= NOW() - INTERVAL '1 hour'
ORDER BY window_start ASC;

-- Query 5: Current Average Temperature (Stat Panel)
-- Shows single value for current temperature
SELECT 
    ROUND(avg_temp::numeric, 2) as value
FROM sensor_metrics
ORDER BY window_start DESC
LIMIT 1;

-- Query 6: Current Average Humidity (Stat Panel)
-- Shows single value for current humidity
SELECT 
    ROUND(avg_humidity::numeric, 2) as value
FROM sensor_metrics
ORDER BY window_start DESC
LIMIT 1;

-- Query 7: Total Events Processed (Stat Panel)
-- Shows total number of events
SELECT 
    SUM(event_count) as value
FROM sensor_metrics
WHERE window_start >= NOW() - INTERVAL '1 hour';

-- Query 8: Temperature Range (Min/Max)
-- Shows temperature variations
SELECT 
    MIN(avg_temp) as min_temp,
    MAX(avg_temp) as max_temp,
    AVG(avg_temp) as avg_temp
FROM sensor_metrics
WHERE window_start >= NOW() - INTERVAL '1 hour';

-- Query 9: Data Freshness Check
-- Shows when last data was received
SELECT 
    window_start as last_update,
    EXTRACT(EPOCH FROM (NOW() - window_start)) as seconds_ago
FROM sensor_metrics
ORDER BY window_start DESC
LIMIT 1;

-- Query 10: Aggregated Stats for Gauge
-- For gauge visualization showing ranges
SELECT 
    ROUND(AVG(avg_temp)::numeric, 2) as avg_temp,
    ROUND(AVG(avg_humidity)::numeric, 2) as avg_humidity
FROM sensor_metrics
WHERE window_start >= NOW() - INTERVAL '10 minutes';
