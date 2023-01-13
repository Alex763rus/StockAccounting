package com.example.stockAccounting.service;

import com.example.stockAccounting.model.mainMenu.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MainMenuService {

    @Autowired
    private MainMenuStart mainMenuStart;
    @Autowired
    private MainMenuDefault mainMenuActivityDefault;

    @Autowired
    private MainMenuBuildObject mainMenuBuildObject;

    @Autowired
    private MainMenuStock mainMenuStock;

    @Autowired
    private MainMenuMaterialConsump mainMenuMaterialConsump;

    private List<MainMenuActivity> mainMenu;

    @PostConstruct
    public void mainMenuInit() {
        mainMenu = new ArrayList();
        mainMenu.add(mainMenuStart);
        mainMenu.add(mainMenuBuildObject);
        mainMenu.add(mainMenuStock);
        mainMenu.add(mainMenuMaterialConsump);
    }

    public PartialBotApiMethod mainMenuRun(Update update) {
        MainMenuActivity mainMenuActivity = null;
        if (update.getMessage() != null) {
            mainMenuActivity = mainMenu.stream()
                    .filter(e -> e.getMenuName().equals(update.getMessage().getText()))
                    .findFirst()
                    .orElse(null);
        }
        if (mainMenuActivity == null) {
            mainMenuActivity = mainMenu.stream()
                    .filter(e -> e.getStatus() != MainMenuStatus.FREE)
                    .findFirst()
                    .orElse(null);
            if (mainMenuActivity == null) {
                log.warn("Not a found command with name: " + update.getMessage().getText());
                mainMenuActivity = mainMenuActivityDefault;
            }
        }
        PartialBotApiMethod mainMenuAnswer = mainMenuActivity.menuRun(update);
        return mainMenuAnswer;
    }

    public List<BotCommand> getMainMenuComands() {
        List<BotCommand> listofCommands = new ArrayList<>();
        for (MainMenuActivity mainMenuActivity : mainMenu) {
            listofCommands.add(new BotCommand(mainMenuActivity.getMenuName(), mainMenuActivity.getDescription()));
        }
        return listofCommands;
    }
}
