-- PostgreSQL initialization script
-- This will be executed when the container starts for the first time

CREATE TABLE IF NOT EXISTS sensor_metrics (
    id SERIAL PRIMARY KEY,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    avg_temp DOUBLE PRECISION,
    avg_humidity DOUBLE PRECISION,
    event_count BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_window_start ON sensor_metrics(window_start DESC);

-- Insert sample data for testing (optional)
INSERT INTO sensor_metrics (window_start, window_end, avg_temp, avg_humidity, event_count)
VALUES 
    (NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '9 minutes 50 seconds', 22.5, 55.0, 100),
    (NOW() - INTERVAL '9 minutes 50 seconds', NOW() - INTERVAL '9 minutes 40 seconds', 23.1, 56.2, 105),
    (NOW() - INTERVAL '9 minutes 40 seconds', NOW() - INTERVAL '9 minutes 30 seconds', 22.8, 54.8, 98);

GRANT ALL PRIVILEGES ON TABLE sensor_metrics TO postgres;
GRANT ALL PRIVILEGES ON SEQUENCE sensor_metrics_id_seq TO postgres;

-- Display confirmation
SELECT 'Database initialized successfully!' AS status;
SELECT 'Sample data inserted' AS status;
