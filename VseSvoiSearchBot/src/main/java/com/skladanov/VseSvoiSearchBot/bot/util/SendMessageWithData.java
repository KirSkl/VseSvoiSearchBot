package com.skladanov.VseSvoiSearchBot.bot.util;

import com.skladanov.VseSvoiSearchBot.bot.model.Request;
import com.skladanov.VseSvoiSearchBot.bot.model.Response;
import lombok.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Getter
@Setter
@NoArgsConstructor
public class SendMessageWithData extends SendMessage {
    private Request request;
    private Response response;
    public SendMessageWithData(String chatId, String text) {
        super(chatId, text);
    }
}
