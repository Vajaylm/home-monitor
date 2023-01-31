package com.vajay.homemonitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.vajay.homemonitor.model.WeatherData;
import com.vajay.homemonitor.model.WeatherResponse;

@Service
@EnableScheduling
public class WeatherService {
    private static final String WEATHER_API_URL = "http://api.openweathermap.org/data/2.5/weather?q={location}&units=metric&appid={apiKey}";
    private String apiKey = System.getenv().get("openWeatherMapApiKey");
    private String location = System.getenv().get("weatherLocation");
    
    private WeatherData outWeatherData;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    public WeatherService(WeatherData outWeatherData) {
        this.outWeatherData = outWeatherData;
    }

    @Scheduled(fixedRate = 3600000)
    public void getWeatherFromApi() {
        WeatherResponse response = null;
        logger.info("Start OpenWeatherMap API call");
        try {
            response = restTemplate.getForObject(WEATHER_API_URL, WeatherResponse.class, location, apiKey);
        } catch (RestClientException e) {
            logger.error("Error while calling weather API: {}", e.getMessage(), e);
        }
        if (response != null) {
            outWeatherData.setTemperature(response.getMain().getTemp());
            outWeatherData.setHumidity(response.getMain().getHumidity());
            outWeatherData.setPressure(response.getMain().getPressure());
            logger.info("Outside weather data from API: {}", outWeatherData);
        }
    }
}
