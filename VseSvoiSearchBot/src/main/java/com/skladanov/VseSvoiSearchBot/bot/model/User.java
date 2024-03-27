package com.skladanov.VseSvoiSearchBot.bot.model;

import com.skladanov.VseSvoiSearchBot.bot.RequestStages;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private RequestStages stage = RequestStages.SPECIALIST_AGE;
    private Boolean isCreationRequest = false;

    public User (Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
