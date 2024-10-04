package com.skladanov.VseSvoiSearchBot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {
    private static final String START = "/start";
    private static final String REQUEST = "/request";
    private static final Long TEST_CHAT_ID = -1002107664061L;
    private final UpdateHandler updateHandler;

    public Bot(@Value("${bot.token}") String botToken, UpdateHandler updateHandler) {
        super(botToken);
        this.updateHandler = updateHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        /*if (update.getMessage().getText().equals("/send")) {
            sendMessage(new SendMessage(String.valueOf(TEST_CHAT_ID), "что-то из БД"));
        }*/
        sendMessage(updateHandler.handleUpdate(update));
    }

    @Override
    public String getBotUsername() {
        return "VSSearch_Bot";
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

}
