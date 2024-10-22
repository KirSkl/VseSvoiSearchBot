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
    private static final String TEST_CHAT_ID = "-1002107664061L";
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
        final var message = updateHandler.handleUpdate(update);
        if (message.getText().equals("Готово! Отправил ваш запрос в чат!")) {
            final var request = updateHandler.getRequest();
            sendMessage(new SendMessage(TEST_CHAT_ID, "Привет!\n" +
                    request.toString()));
        }
        if (message.getText().equals("Отклик отправлен!")) {
            final var response = updateHandler.getResponse();
            sendMessage(new SendMessage(response.getRequest().getUser().getId().toString(),
                    "Привет! Новый отклик на ваш запрос:\n" +
                            response));
        }
        sendMessage(message);
    }

    @Override
    public String getBotUsername() {
        return "VSSearch_Bot";
    }

    private void sendMessage(SendMessage message) {
        try {
            var msgId = execute(message).getMessageId(); //в дальнейшем использовать id для редактирования сообщения
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }


}
