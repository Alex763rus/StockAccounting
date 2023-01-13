package com.example.stockAccounting.model.mainMenu;

import com.example.stockAccounting.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class MainMenuDefault implements MainMenuActivity {

    final String MENU_NAME = "/default";

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public PartialBotApiMethod menuRun(Update update) {
        String chatId = String.valueOf(update.getMessage().getChatId());

        String command = update.getMessage().getText();
        return new SendMessage(chatId, "..!.. Not a found command with name: " + command);
    }

    @Override
    public String getDescription() {
        return MENU_NAME;
    }

}
