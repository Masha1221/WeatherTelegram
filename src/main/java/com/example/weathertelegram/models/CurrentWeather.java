package com.example.weathertelegram.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeather {

    private String country;
    private String city;
    private String weather;
    private double temperature;
    private double feelsLike;
    private double windSpeed;
    private double pressure;
    private double humidity;
}