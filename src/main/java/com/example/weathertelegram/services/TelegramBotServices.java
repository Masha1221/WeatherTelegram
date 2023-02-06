package com.example.weathertelegram.services;

import com.example.weathertelegram.configs.TelegramBotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@EnableScheduling
public class TelegramBotServices extends TelegramLongPollingBot {

    final TelegramBotConfig telegramBotConfig;

    final CurrentWeatherServices currentWeatherServices;

    public TelegramBotServices(TelegramBotConfig telegramBotConfig, CurrentWeatherServices currentWeatherServices) {
        this.telegramBotConfig = telegramBotConfig;
        this.currentWeatherServices = currentWeatherServices;
    }

    @Override
    public String getBotUsername() {
        return telegramBotConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return telegramBotConfig.getBotKey();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {

                case "/start":
                    startCommandReceived(chatId);
                    break;

                case "/viewWeatherNow":
                    commandReceivedGetWeather(chatId);
                    break;
            }
        }
    }

    private void startCommandReceived(long chatId) {
        String answer = "Hi! I will show you the weather in Kyiv:) " +
                "Enter /viewWeatherNow  if you wanna to know weather wright now.";
        sendMessage(chatId, answer);
    }

    private void commandReceivedGetWeather(long chatId) {
        String answer = currentWeatherServices.getCurrentWeather();
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException ignored) {
        }
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Europe/Kyiv")
    public void gettingTheWeatherEveryHour() throws TelegramApiException {
        String groupId = "@Hi18090";
        String message = currentWeatherServices.getCurrentWeather();
        execute(new SendMessage(groupId, message));
    }
}
