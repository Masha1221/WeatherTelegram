package com.example.battle_bot.services;

import com.example.battle_bot.exceptions.UploadFileException;
import com.example.battle_bot.models.ImageEntity;
import com.example.battle_bot.models.PostEntity;
import com.example.battle_bot.models.UserEntity;
import com.example.battle_bot.models.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotService extends TelegramLongPollingBot {

    private final Map<Long, PostEntity> postText = new HashMap<>();
    private final Map<Long, PostEntity> postTextImageMap = new HashMap<>();

    private final PostEntity post = new PostEntity();
    private boolean isTextReceived = false;
    private PostEntity postTextImage;
    private final ImageService imageService;
    private final UserService userService;
    private final PostService postService;

    private final InstagramService instagramService;

    @Override
    public String getBotUsername() {
        return Value.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return Value.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();
        userService.saveNotExistingUser(message.getChatId(), message);

        if (update.hasMessage() && update.getMessage().hasText()) {
            boolean isMemberTelegram = isTelegramSubscriber(update);
            long chatId = update.getMessage().getChatId();
            PostEntity postText = this.postText.get(chatId);
            String messageText = update.getMessage().getText();

            switch (messageText) {
                case "/start":
                    sendMessage(chatId, Value.COMMAND_START);
                    createTwoBoard(chatId, Value.COMMAND_GET_SUPPORT,
                            Value.COMMAND_GET_BONUS_CODE);
                    break;
                case Value.COMMAND_MENU:
                    createTwoBoard(chatId, Value.COMMAND_GET_SUPPORT,
                            Value.COMMAND_GET_BONUS_CODE);
                    break;

                case Value.COMMAND_GET_SUPPORT:
                    sendMessage(chatId, "Подключаем чат оператора.. Опишите пожалуйста ваш" +
                            " вопрос, мы обязательно вам поможем!");
                    break;

                case Value.COMMAND_GET_BONUS_CODE:
                    sendMessage(chatId, Value.COMMAND_CONDITIONS_BONUS_CODE);
                    createTwoBoard(chatId, Value.COMMAND_I_DID_CONDITIONS_TELEGRAM,
                            Value.COMMAND_I_DID_CONDITIONS_INSTAGRAM);
                    break;

                case Value.COMMAND_GET_COUNT_USERS:
                    sendMessage(chatId, "В текущий момент количество пользователей: "
                            + userService.getAllUsers().size());
                    break;

                case Value.COMMAND_CREATE_NEWSLETTER:
                    sendMessage(chatId, Value.COMMAND_START_NEWSLETTER);
                    createTwoBoard(chatId, Value.COMMAND_1_FORM, Value.COMMAND_2_FORM);
                    break;

                case "Меню":
                    createTwoBoard(chatId
                            , Value.COMMAND_GET_COUNT_USERS, Value.COMMAND_CREATE_NEWSLETTER);
                    break;

                case "Отправить рассылку":
                    PostEntity postEntity = postService.getTheLastPost();
                    sendMessageToAllUsers(postEntity.getDescription());
                    break;

                case "Отправить рассылку Текст\u27A1Изображение":
                    sendMessageWithImageToAllUsers();
                    break;
            }

            //проверка условий и выдача бонус кода
            if (messageText.equals(Value.COMMAND_I_DID_CONDITIONS_TELEGRAM) && isMemberTelegram) {
                //TODO  добавить логику выдачи бонус кода
                UserEntity user = userService.findByChatId(chatId);
                if (!user.isBonusCodeGivenTelegram()) {
                    Date date = new Date();
                    sendMessage(chatId, "Отлично! Получите ваш бонус-код - iwefn84bru3ibn.");
                    user.setBonusCodeGivenTelegram(true);
                    user.setDateOfGivenBonusCodeTelegram(date);
                    userService.save(user);
                    createOneBoard(chatId, Value.COMMAND_MENU, Value.CHOOSE_COMMAND);
                } else {
                    UserEntity userEntity = userService.findByChatId(chatId);
                    Date dateBonusCodeOfTelegram = userEntity.getDateOfGivenBonusCodeTelegram();
                    Date currentDate = new Date();
                    userEntity.setBonusCodeGivenTelegram(true);
                    userEntity.setDateOfGivenBonusCodeTelegram(currentDate);
                    userService.save(userEntity);
                    conditionsDate(chatId, dateBonusCodeOfTelegram, currentDate);
                }
            } else if (messageText.equals(Value.COMMAND_I_DID_CONDITIONS_TELEGRAM)) {
                sendMessage(chatId, "Извините, вы не подписались на Телеграм канал.");
            }

            if (messageText.equals(Value.COMMAND_I_DID_CONDITIONS_INSTAGRAM)) {
                giveBonusCodeInstagram(chatId);
            }

            if (messageText.equals("/admin")) {
                if ((message.getFrom().getId().equals(Value.ID_ADMIN_1)) ||
                        (message.getFrom().getId().equals(Value.ID_ADMIN_2))) {
                    sendMessage(chatId, Value.COMMAND_GREETINGS_FOR_ADMINISTRATOR);
                    createTwoBoard(chatId, Value.COMMAND_GET_COUNT_USERS, Value.COMMAND_CREATE_NEWSLETTER);
                } else {
                    sendMessage(chatId, "Извините, у вас нет прав администратора.");
                }
            }

            //постТекст
            if (messageText.equals(Value.COMMAND_1_FORM)) {
                sendMessage(chatId, "Пожалуйста отправьте текст");
                postText = new PostEntity();
                postText.setChatId(chatId);
                postText.setForm("Text");
                this.postText.put(chatId, postText);

            } else if (postText != null && postText.getDescription() == null && postText.getForm().equals("Text")) {
                postText.setDescription(messageText);
                postService.savePost(postText);
                sendMessage(chatId, "Отлично! Текст сохранен.");
                PostEntity postEntity = postService.getTheLastPost();
                sendMessage(chatId, "Ваш пост: \n " + postEntity.getDescription());
                createTwoBoard(chatId, "Меню", "Отправить рассылку");
            }


            //постТекст->Изображение
            if (messageText.equals(Value.COMMAND_2_FORM)) {
                sendMessage(chatId, "Отправьте текст, а потом изображение");
                postTextImage = new PostEntity();
                postTextImage.setChatId(chatId);
                postTextImage.setForm("TextImage");
                this.postTextImageMap.put(chatId, postTextImage);

            } else if (postTextImage != null
                    && postTextImage.getDescription() == null
                    && postTextImage.getForm().equals("TextImage")) {

                postTextImage.setDescription(messageText);
                postService.savePost(postTextImage);
                sendMessage(chatId, "Отлично! Текст сохранен!");
                PostEntity post = postService.getTheLastPost();
                sendMessage(chatId, "Ваш текст в посте:\n \n" + post.getDescription());
                sendMessage(chatId, "Теперь пришлите изображение");
                isTextReceived = true;

            }
        } else if (update.getMessage().hasPhoto()) {
            long chatId = update.getMessage().getChatId();
            if (isTextReceived) {
                try {
                    PostEntity postEntity = postService.getTheLastPost();
                    ImageEntity imageEntity = imageService.savePhoto(update.getMessage());
                    postEntity.setImage(imageEntity);
                    postService.savePost(postEntity);
                    InputFile inputFile = new InputFile(new ByteArrayInputStream(imageService
                            .getTheLastBinaryContextEntity()
                            .getFileAsArrayOfBytes()), "photo.jpg");
                    sendMessage(chatId, "Изображение успешно сохранено!\n" + "Ваш пост:\n");
                    sendImageToUser(chatId, postEntity.getDescription(), inputFile);
                    createTwoBoard(chatId, "Меню", "Отправить рассылку Текст\u27A1Изображение");
                } catch (TelegramApiException | IOException | URISyntaxException | UploadFileException e) {
                    String error = "К сожелению загрузка фото не удалась. Повторите попытку позже.";
                    sendMessage(chatId, error);
                }
            } else {
                sendMessage(chatId, "Сначала отправьте текст, затем изображение.");
            }
        }
    }

    private void conditionsDate(long chatId, Date dateBonusCode, Date currentDate) {
        long diffInMillis = currentDate.getTime() - dateBonusCode.getTime();
        long thirtyDaysInMillis = 30L * 24L * 60L * 60L * 1000L;
        if (diffInMillis >= thirtyDaysInMillis) {
            sendMessage(chatId, "Отлично , получите ваш новый бонус-код! jwefnjoeifio39");
            createOneBoard(chatId, Value.COMMAND_MENU, Value.CHOOSE_COMMAND);
        } else {
            sendMessage(chatId, "Извините, вы уже получили бонус код ранее.\n" +
                    "Следующий бонус будет доступен уже через " + String.format("%d дней и %d часов",
                    TimeUnit.MILLISECONDS.toDays(thirtyDaysInMillis - diffInMillis),
                    TimeUnit.MILLISECONDS.toHours(thirtyDaysInMillis - diffInMillis) % 24L) + ".");
            createOneBoard(chatId, Value.COMMAND_MENU, Value.CHOOSE_COMMAND);
        }
    }

    private void giveBonusCodeInstagram(long chatId) {
        UserEntity user = userService.findByChatId(chatId);
        if (!user.isBonusCodeGivenInstagram()) {
            Date date = new Date();
            sendMessage(chatId, "Супер! Получите ваш бонус код - 2jf383uhff3iknf");
            user.setBonusCodeGivenInstagram(true);
            user.setDateOfGivenBonusCodeInstagram(date);
            userService.save(user);
            createOneBoard(chatId, Value.COMMAND_MENU, Value.CHOOSE_COMMAND);
        } else {
            UserEntity userEntity = userService.findByChatId(chatId);
            Date dateBonusCodeOfInstagram = userEntity.getDateOfGivenBonusCodeInstagram();
            Date currentDate = new Date();
            userEntity.setBonusCodeGivenInstagram(true);
            userEntity.setDateOfGivenBonusCodeInstagram(currentDate);
            userService.save(userEntity);
            conditionsDate(chatId, dateBonusCodeOfInstagram, currentDate);
        }
    }

    private boolean isTelegramSubscriber(Update update) {
        long chatId = update.getMessage().getChatId();
        GetChatMember getChatMember = new GetChatMember(Value.ID_GROUP_THE_BATTLE_CLUB, chatId);
        try {
            ChatMember chatMember = execute(getChatMember);
            return chatMember.getStatus().equals("member") || chatMember.getStatus().equals("creator") || chatMember.getStatus().equals("administrator");
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createNewBoard(long chatId, ReplyKeyboardMarkup keyboard, List<KeyboardRow> keyboardRows, KeyboardRow row3) {
        keyboardRows.add(row3);
        keyboard.setKeyboard(keyboardRows);
        keyboard.setOneTimeKeyboard(true);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Выберите пункт меню\uD83D\uDC47");
        sendMessage.setReplyMarkup(keyboard);

        try {
            execute(sendMessage);
        } catch (
                TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void createOneBoard(long chatId, String theFirstBoard, String text) {

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(theFirstBoard);
        keyboardRows.add(row1);
        keyboard.setKeyboard(keyboardRows);
        keyboard.setOneTimeKeyboard(true);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);

        try {
            execute(sendMessage);
        } catch (
                TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void createTwoBoard(long chatId, String theFirstBoard, String theSecondBoard) {

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(theFirstBoard);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(theSecondBoard);

        keyboardRows.add(row1);
        createNewBoard(chatId, keyboard, keyboardRows, row2);
    }


    private void sendMessageWithImageToAllUsers() {
        for (UserEntity user : userService.getAllUsers()) {
            PostEntity post = postService.getTheLastPost();
            InputFile photo = new InputFile(new ByteArrayInputStream(imageService.getTheLastBinaryContextEntity().getFileAsArrayOfBytes()), "photo.jpg");

            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(String.valueOf(user.getChatId()));
            sendPhoto.setCaption(post.getDescription());
            sendPhoto.setPhoto(photo);
            try {
                execute(sendPhoto);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessageToAllUsers(String caption) {
        for (UserEntity user : userService.getAllUsers()) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(user.getChatId()));
            message.setText(caption);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendImageToUser(Long chatId, String caption, InputFile inputFile) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setCaption(caption);
        sendPhoto.setPhoto(inputFile);

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
}

