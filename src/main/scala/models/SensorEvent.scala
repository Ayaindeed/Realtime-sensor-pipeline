package models

case class SensorEvent(
  sensorId: Int,
  temperature: Double,
  humidity: Double,
  timestamp: Long
)
