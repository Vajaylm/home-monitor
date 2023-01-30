package com.vajay.homemonitor.service;

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
public class MqttMessageHandler implements MessageHandler{
    private static final Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);
    private WeatherData inWeatherData;
    
    public MqttMessageHandler(WeatherData inWeatherData) {
        this.inWeatherData = inWeatherData;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        if (message != null) {
            logger.info("MQTT message arrived: {}", message.getPayload());
            try {
                inWeatherData = new ObjectMapper().readValue(message.getPayload().toString(), WeatherData.class);
                logger.info("The conversion of message to WeatherData is successful");
                logger.info("New inner weather data: {}", inWeatherData);
            } catch (JsonProcessingException e) {
                logger.error("Error occured while converting message to WeatherData: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
