package com.skladanov.VseSvoiSearchBot.bot;

import com.skladanov.VseSvoiSearchBot.bot.model.Request;
import com.skladanov.VseSvoiSearchBot.bot.model.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
class UpdateHandler {
    private final BotService botService;

    public SendMessage handleUpdate(Update update) {
        return botService.makeAnswer(update);
    }

    public Request getRequest() {
        return botService.getRequest();
    }

    public Response getResponse() {
        return botService.getResponse();
    }
}
