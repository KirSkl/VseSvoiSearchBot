package com.skladanov.VseSvoiSearchBot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotService {

    SendMessage makeAnswer(Update update);
}
