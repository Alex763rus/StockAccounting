package com.example.stockAccounting.service;

import com.example.stockAccounting.config.BotConfig;
import com.example.stockAccounting.model.jpa.Stock;
import com.example.stockAccounting.model.mainMenu.MainMenuStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.List;


@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    final long ADMIN_CHAT_ID = 799008767;
    @Autowired
    private BotConfig botConfig;

    @Autowired
    private MainMenuService mainMenuService;

    @PostConstruct
    public void init() {
        try {
            execute(new SetMyCommands(mainMenuService.getMainMenuComands(), new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
        log.info("==" + "Server was starded. Version: " + botConfig.getBotVersion() + "==================================================================================================");
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            PartialBotApiMethod answer = mainMenuService.mainMenuRun(update);
            try {
                if (answer instanceof BotApiMethod) {
                    execute((BotApiMethod) answer);
                }
                if (answer instanceof SendDocument) {
                    deleteLastMessage(update);
                    execute((SendDocument) answer);
                }
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());
            }
        }
    }

    private void deleteLastMessage(Update update) throws TelegramApiException {
        EditMessageText editMessageText = new EditMessageText();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        editMessageText.setChatId(String.valueOf(chatId));
        editMessageText.setMessageId((int) messageId);
        editMessageText.setText("Документ готов!");
        execute(editMessageText);
    }
}
