#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_BME280.h>
#include <ESP8266WiFi.h>
#include <Ticker.h>
#include "defines.h"

WiFiEventHandler wifiConnectHandler;
WiFiEventHandler wifiDisconnectHandler;
Ticker wifiReconnectTimer;

Adafruit_BME280 bme;
float temperature;
float humidity;
float pressure;

void connectToWifi()
{
    Serial.println("Connecting to Wi-Fi...");
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
}

void onWifiConnect(const WiFiEventStationModeGotIP &event)
{
    Serial.println("Connected to Wi-Fi.");
}

void onWifiDisconnect(const WiFiEventStationModeDisconnected &event)
{
    Serial.println("Disconnected from Wi-Fi.");
    wifiReconnectTimer.once(2, connectToWifi);
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

    connectToWifi();
}

void loop()
{
    temperature = bme.readTemperature();
    humidity = bme.readHumidity();
    pressure = bme.readPressure() / 100.0F;

    Serial.print("Temperature: ");
    Serial.println(temperature);
    Serial.print("Humidity: ");
    Serial.println(humidity);
    Serial.print("Pressure: ");
    Serial.println(pressure);

    delay(2000);
}