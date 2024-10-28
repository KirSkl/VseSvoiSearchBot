package com.skladanov.VseSvoiSearchBot.bot;

import com.skladanov.VseSvoiSearchBot.bot.model.Request;
import com.skladanov.VseSvoiSearchBot.bot.model.Response;
import com.skladanov.VseSvoiSearchBot.bot.util.SendMessageWithData;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotService {

    SendMessageWithData makeAnswer(Update update);
}
