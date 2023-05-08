package com.example.battle_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;


@SpringBootApplication
public class TheBattleTelegramApplication {
    public static void main(String[] args) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        SpringApplication.run(TheBattleTelegramApplication.class, args);
    }
}


