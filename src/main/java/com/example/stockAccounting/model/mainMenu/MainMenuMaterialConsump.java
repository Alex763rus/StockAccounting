package com.example.stockAccounting.model.mainMenu;

import com.example.stockAccounting.model.jpa.*;
import com.example.stockAccounting.service.ButtonService;
import com.example.stockAccounting.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class MainMenuMaterialConsump implements MainMenuActivity {

    final String MENU_NAME = "/consump";

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private MaterialConsumpRepository materialConsumpRepository;
    @Autowired
    private BuildObjectRepository buildObjectRepository;
    @Autowired
    private ButtonService buttonService;
    @Autowired
    private MaterialRepository materialRepository;
    private MainMenuStatus mainMenuStatus = MainMenuStatus.FREE;

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public String getDescription() {
        return "Добавить расход материала";
    }

    @Autowired
    ExcelService excelService;
    private HashMap<String, String> btnsTmp = new HashMap<>();
    private MaterialConsump materialConsumpTmp;

    @Override
    public PartialBotApiMethod menuRun(Update update) {
        PartialBotApiMethod answer = null;
        switch (mainMenuStatus) {
            case FREE:
                answer = freeLogic(update);
                break;
            case CONSUMP_WAIT_OBJECT:
                answer = consumpWaitObjectLogic(update);
                break;
            case CONSUMP_WAIT_MATERIAL:
                answer = consumpWaitMaterialLogic(update);
                break;
            case CONSUMP_WAIT_MATERIAL_CNT:
                answer = consumpWaitMaterialCntLogic(update);
                break;
        }
        return answer;
    }
    private BotApiMethod freeLogic(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText("Выберите объект:");
        List<BuildObject> buildObjectList = (List<BuildObject>) buildObjectRepository.findAll();
        btnsTmp.clear();
        for (int i = 0; i < buildObjectList.size(); ++i) {
            btnsTmp.put(buildObjectList.get(i).getBuildObjectId().toString(), buildObjectList.get(i).getName());
        }
        sendMessage.setReplyMarkup(buttonService.createVerticalMenu(btnsTmp));
        mainMenuStatus = MainMenuStatus.CONSUMP_WAIT_OBJECT;
        return sendMessage;
    }
    private BotApiMethod consumpWaitObjectLogic(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        if (update.hasCallbackQuery()) {
            materialConsumpTmp = new MaterialConsump();
            BuildObject buildObject = buildObjectRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).orElse(null);
            materialConsumpTmp.setBuildObject(buildObject);
//            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId((int) messageId);
            editMessageText.setText("Выбран объект:" + buildObject.getName() + "\r\n" +
                                    "Выберите материал:");
            List<Material> materialList = (List<Material>) materialRepository.findAll();
            btnsTmp.clear();
            for (int i = 0; i < materialList.size(); ++i) {
                btnsTmp.put(materialList.get(i).getMaterialId().toString(), materialList.get(i).getName());
            }
            editMessageText.setReplyMarkup(buttonService.createVerticalMenu(btnsTmp));
            mainMenuStatus = MainMenuStatus.CONSUMP_WAIT_MATERIAL;
        }
        return editMessageText;
    }
    private BotApiMethod consumpWaitMaterialLogic(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        if (update.hasCallbackQuery()) {
            Material material = materialRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).orElse(null);
            materialConsumpTmp.setMaterial(material);
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId((int) messageId);
            mainMenuStatus = MainMenuStatus.CONSUMP_WAIT_MATERIAL_CNT;
            editMessageText.setText("Выбран объект: " + materialConsumpTmp.getBuildObject().getName() + "\r\n " +
                                    "Выбран материал: " + material.getName()+ "\r\n " +
                                    "Введите количество:\n");
        }
        return editMessageText;
    }
    private BotApiMethod consumpWaitMaterialCntLogic(Update update) {
        materialConsumpTmp.setCnt(Double.parseDouble(update.getMessage().getText()));
        mainMenuStatus = MainMenuStatus.FREE;
        String chatId = String.valueOf(update.getMessage().getChatId());
        materialConsumpRepository.save(materialConsumpTmp);
        return new SendMessage(chatId, "Новая трата успешно сохранена по объекту: \r\n"
                + materialConsumpTmp.getBuildObject().getName() + " \r\n"
                + materialConsumpTmp.getMaterial().getName() + " \r\n"
                + ", количество:" +  materialConsumpTmp.getCnt());
    }
    public MainMenuStatus getStatus() {
        return mainMenuStatus;
    }

}
