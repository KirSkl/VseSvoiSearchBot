package com.skladanov.VseSvoiSearchBot.bot;

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
}
