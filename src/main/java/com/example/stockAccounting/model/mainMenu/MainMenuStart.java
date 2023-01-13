package com.example.stockAccounting.model.mainMenu;

import com.example.stockAccounting.service.database.UserService;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class MainMenuStart implements MainMenuActivity{

    final String MENU_NAME = "/start";

    @Autowired
    private UserService userService;

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public SendMessage menuRun(Update update) {
        userService.registeredUser(update.getMessage());
        String userName = update.getMessage().getChat().getFirstName();
        log.info("Replied to user " + userName);
        String answer = EmojiParser.parseToUnicode("Hello, " + userName + "!" + " :blush:");
        String chatId = String.valueOf(update.getMessage().getChatId());
        return new SendMessage(chatId,  answer);
    }

    @Override
    public String getDescription() {
        return " Поехали!";
    }
}
