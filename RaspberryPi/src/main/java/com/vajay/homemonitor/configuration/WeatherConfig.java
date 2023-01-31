package com.vajay.homemonitor.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vajay.homemonitor.model.WeatherData;

@Configuration
public class WeatherConfig {
    @Bean
    public WeatherData inWeatherData() {
        return new WeatherData();
    }

    @Bean
    public WeatherData outWeatherData() {
        return new WeatherData();
    }
}
