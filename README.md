# Home monitor

## Goal
Measure temperature, humidity and pressure in different locations of the house.

## Tools
- BME280 sensor
- NodeMCU to control sampling of the sensor and forward data
- Raspberry Pi 4 to run MQTT broker
- Java application to get sensor data from MQTT topic and send it to Google Sheets, additionally a timestamp and outer temperature
