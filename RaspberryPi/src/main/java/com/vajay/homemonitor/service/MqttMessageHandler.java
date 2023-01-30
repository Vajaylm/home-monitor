package com.vajay.homemonitor.service;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vajay.homemonitor.model.WeatherData;

@Service
public class MqttMessageHandler implements MessageHandler{
    
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        if (message != null) {
            WeatherData data = null;
            try {
                data = new ObjectMapper().readValue(message.getPayload().toString(), WeatherData.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            System.out.println(data);
        }
    }
}
