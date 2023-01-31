# Home monitor

## Goal
Measure temperature, humidity and pressure in bedroom.

## Tools
- BME280 sensor
- NodeMCU to control sampling of the sensor and forward data
- Raspberry Pi 4 to run MQTT broker and Java app
- Java application to get sensor data from MQTT topic and send it to Google Sheets, additionally a timestamp and outer temperature

## Resources
- Rui Santos, www.randomnerdtutorials.com NodeMCU code parts
- OpenAI ChatGPT
