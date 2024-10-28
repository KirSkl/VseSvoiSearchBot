package com.skladanov.VseSvoiSearchBot.bot;

import com.skladanov.VseSvoiSearchBot.bot.util.SendMessageWithData;
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
    private static final String TEST_CHAT_ID = "-1002107664061L";
    private final UpdateHandler updateHandler;

    public Bot(@Value("${bot.token}") String botToken, UpdateHandler updateHandler) {
        super(botToken);
        this.updateHandler = updateHandler;
    }

    @Override
    public String getBotUsername() {
        return "VSSearch_Bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        final var message = updateHandler.handleUpdate(update);
        processMessageContent(message);
        sendMessage(message);
    }

    private void processMessageContent(SendMessageWithData message) {
        if (message.getRequest() != null) {
            sendRequestMessage(message);
        }
        if (message.getResponse() != null) {
            sendResponseMessage(message);
        }
    }

    private void sendRequestMessage(SendMessageWithData message) {
        String requestMessage = "Привет!\n" + message.getRequest();
        sendMessage(new SendMessage(TEST_CHAT_ID, requestMessage));
    }

    private void sendResponseMessage(SendMessageWithData message) {
        String userId = message.getResponse().getRequest().getUser().getId().toString();
        String responseMessage = "Привет! Новый отклик на ваш запрос:\n" + message.getResponse();
        sendMessage(new SendMessage(userId, responseMessage));
    }

    private void sendMessage(SendMessage message) {
        try {
            var msgId = execute(message).getMessageId(); //в дальнейшем использовать id для редактирования сообщения
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}
