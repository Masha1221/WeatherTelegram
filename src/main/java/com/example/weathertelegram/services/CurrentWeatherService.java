package com.example.weathertelegram.services;

import com.example.weathertelegram.models.CurrentWeather;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;

@Service
@Slf4j
@Component
public class CurrentWeatherService {

    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q={city}," +
            "{country}&APPID={apiKey}&units=imperial";

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @Value("${OPENWEATHERMAP_KEY}")
    private String apiKey;

    public CurrentWeatherService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    public CurrentWeather getCurrentWeather(String city, String country) {
        final URI url = new UriTemplate(WEATHER_URL).expand(city, country, apiKey);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return convert(response);
    }

    public String getCurrentWeather() {
        CurrentWeather currentWeather = getCurrentWeather("Kyiv", "UA");
        return currentWeather.getCity() + " , " + currentWeather.getCountry() + "\n"
                + "weather:  " + currentWeather.getWeather() + "\n"
                + "temperature:  " + (int) currentWeather.getTemperature() + " °C" + "\n"
                + "feels like:  " + (int) currentWeather.getFeelsLike() + " °C" + "\n"
                + "windSpeed:  " + (int) currentWeather.getWindSpeed() + " meters/sec" + "\n"
                + "pressure:  " + (int) currentWeather.getPressure() + " hPa" + "\n"
                + "humidity:  " + (int) currentWeather.getHumidity() + " %";
    }

    private CurrentWeather convert(ResponseEntity<String> response) {
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            CurrentWeather currentWeatherDto = new CurrentWeather
                    (root.path("sys").path("country").asText(),
                            root.path("name").asText(),
                            root.path("weather").get(0).path("main").asText(),
                            Double.parseDouble(String.valueOf(root.path("main").path("temp"))),
                            Double.parseDouble(String.valueOf(root.path("main").path("feels_like").asDouble())),
                            Double.parseDouble(String.valueOf(root.path("wind").path("speed").asDouble())),
                            Double.parseDouble(String.valueOf(root.path("main").path("pressure").asDouble())),
                            Double.parseDouble(String.valueOf(root.path("main").path("humidity").asDouble())));

            currentWeatherDto.setTemperature(convertFahrenheitToCelsius(currentWeatherDto.getTemperature()));
            currentWeatherDto.setFeelsLike(convertFahrenheitToCelsius(currentWeatherDto.getFeelsLike()));

            return currentWeatherDto;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }

    private static int convertFahrenheitToCelsius(Double fahrenheit) {
        return (int) Math.round((fahrenheit - 32) / 1.8);
    }
}
