package com.example.weathertelegram.configs;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
@Data
public class TelegramBotConfig {

    @Value("${BOT_NAME}")
   private String botName;

    @Value("${BOT_TOKEN}")
    private String botKey;
}
