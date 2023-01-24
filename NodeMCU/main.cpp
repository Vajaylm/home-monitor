#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BME280.h>
#include <ESP8266WiFi.h>
#include <Ticker.h>
#include <AsyncMqttClient.h>
#include "defines.h"

AsyncMqttClient mqttClient;
Ticker mqttReconnectTimer;

WiFiEventHandler wifiConnectHandler;
WiFiEventHandler wifiDisconnectHandler;
Ticker wifiReconnectTimer;

Adafruit_BME280 bme;
float temperature;
float humidity;
float pressure;

unsigned long previousPublish = 0;
const long interval = 10000;

void connectToWifi()
{
    Serial.println("Connecting to Wi-Fi...");
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
}

void connectToMqtt()
{
    Serial.println("Connecting to MQTT...");
    mqttClient.connect();
}

void onWifiConnect(const WiFiEventStationModeGotIP &event)
{
    Serial.println("Connected to Wi-Fi.");
    connectToMqtt();
}

void onWifiDisconnect(const WiFiEventStationModeDisconnected &event)
{
    Serial.println("Disconnected from Wi-Fi.");
    mqttReconnectTimer.detach(); // ensure we don't reconnect to MQTT while reconnecting to Wi-Fi
    wifiReconnectTimer.once(2, connectToWifi);
}

void onMqttConnect(bool sessionPresent)
{
    Serial.println("Connected to MQTT.");
    Serial.print("Session present: ");
    Serial.println(sessionPresent);
}

void onMqttDisconnect(AsyncMqttClientDisconnectReason reason)
{
    Serial.println("Disconnected from MQTT.");

    if (WiFi.isConnected())
    {
        mqttReconnectTimer.once(2, connectToMqtt);
    }
}

void onMqttPublish(uint16_t packetId)
{
    Serial.print("Publish acknowledged.");
    Serial.print("  packetId: ");
    Serial.println(packetId);
}

void setup()
{
    Serial.begin(115200);
    Serial.println();

    if (!bme.begin(0x76))
    {
        Serial.println("Could not find a valid BME280 sensor, check wiring!");
        ESP.restart();
    }
    bme.setTemperatureCompensation(-0.9);

    wifiConnectHandler = WiFi.onStationModeGotIP(onWifiConnect);
    wifiDisconnectHandler = WiFi.onStationModeDisconnected(onWifiDisconnect);

    mqttClient.onConnect(onMqttConnect);
    mqttClient.onDisconnect(onMqttDisconnect);
    mqttClient.onPublish(onMqttPublish);
    mqttClient.setServer(MQTT_HOST, MQTT_PORT);
    mqttClient.setCredentials(MQTT_USER, MQTT_PASSWORD);
    connectToWifi();
}

void loop()
{
    unsigned long currentMillis = millis();
    if (currentMillis - previousPublish >= interval)
    {
        previousPublish = currentMillis;
        
        // BME280 sensor readings
        temperature = bme.readTemperature();
        humidity = bme.readHumidity();
        pressure = bme.readPressure() / 100.0F;

        String publishData = "{\"temperature\": " + String(temperature) + ", \"humidity\": " + String(humidity) + ", \"pressure\": " + String(pressure) + "}";

        // Publish an MQTT message
        Serial.println("Message: " + publishData);
        int packetIdPub = mqttClient.publish(MQTT_PUB_SENSOR, 1, true, publishData.c_str());
        Serial.printf("Publishing on topic %s at QoS 1, packetId: %i ", MQTT_PUB_SENSOR, packetIdPub);
    }    
}
