package com.example.stockAccounting.model.mainMenu;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;


public interface MainMenuActivity {

    public String getMenuName();

    public String getDescription();

    public PartialBotApiMethod menuRun(Update update);

    public default MainMenuStatus getStatus() {
        return MainMenuStatus.FREE;
    }
}
