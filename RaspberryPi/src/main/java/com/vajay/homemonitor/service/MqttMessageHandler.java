package com.vajay.homemonitor.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vajay.homemonitor.model.WeatherData;

@Service
public class MqttMessageHandler implements MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);
    private WeatherData inWeatherData;
    private final GoogleSheetService googleSheetService;

    public MqttMessageHandler(WeatherData inWeatherData, GoogleSheetService googleSheetService) {
        this.inWeatherData = inWeatherData;
        this.googleSheetService = googleSheetService;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        if (message != null) {
            logger.info("MQTT message arrived: {}", message.getPayload());
            try {
                WeatherData data = new ObjectMapper().readValue(message.getPayload().toString(), WeatherData.class);
                inWeatherData.setTemperature(data.getTemperature());
                inWeatherData.setHumidity(data.getHumidity());
                inWeatherData.setPressure(data.getPressure());
                logger.info("The conversion of message to WeatherData is successful");
                logger.info("New inner weather data: {}", inWeatherData);
            } catch (JsonProcessingException e) {
                logger.error("Error occured while converting message to WeatherData: {}", e.getMessage());
                e.printStackTrace();
            }

            try {
                googleSheetService.appendData();
            } catch (GeneralSecurityException e) {
                logger.error("General Security Exception occured while writing to Google Sheet: {}", e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("IO Exception occured while writing to Google Sheet: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
