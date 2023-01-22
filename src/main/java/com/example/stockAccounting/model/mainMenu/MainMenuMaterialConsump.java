package com.example.stockAccounting.model.mainMenu;

import com.example.stockAccounting.enums.State;
import com.example.stockAccounting.model.jpa.*;
import com.example.stockAccounting.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MainMenuMaterialConsump extends MainMenu {

    final String MENU_NAME = "/consump";

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private MaterialConsumpRepository materialConsumpRepository;
    @Autowired
    private BuildObjectRepository buildObjectRepository;
    @Autowired
    private MaterialRepository materialRepository;

    private Map<User, MaterialConsump> materialTmp = new HashMap<>();

    @Override
    public PartialBotApiMethod menuRun(User user, Update update) {
        PartialBotApiMethod answer = null;
        State state = stateService.getState(user);
        switch (state) {
            case FREE:
                answer = freeLogic(user, update);
                break;
            case CONSUMP_WAIT_OBJECT:
                answer = consumpWaitObjectLogic(user, update);
                break;
            case CONSUMP_WAIT_MATERIAL:
                answer = consumpWaitMaterialLogic(user, update);
                break;
            case CONSUMP_WAIT_MATERIAL_CNT:
                answer = consumpWaitMaterialCntLogic(user, update);
                break;
        }
        return answer;
    }
    private BotApiMethod freeLogic(User user, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText("Выберите объект:");
        List<BuildObject> buildObjectList = (List<BuildObject>) buildObjectRepository.findAll();
        HashMap<String, String> btnsTmp = new HashMap<>();
        for (int i = 0; i < buildObjectList.size(); ++i) {
            btnsTmp.put(buildObjectList.get(i).getBuildObjectId().toString(), buildObjectList.get(i).getName());
        }
        sendMessage.setReplyMarkup(buttonService.createVerticalMenu(btnsTmp));
        stateService.setState(user, State.CONSUMP_WAIT_OBJECT);
        return sendMessage;
    }
    private BotApiMethod consumpWaitObjectLogic(User user, Update update) {
        EditMessageText editMessageText = new EditMessageText();
        if (update.hasCallbackQuery()) {
            BuildObject buildObject = buildObjectRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).get();
            materialTmp.put(user, new MaterialConsump());
            materialTmp.get(user).setBuildObject(buildObject);
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId((int) messageId);
            editMessageText.setText("Выбран объект:" + buildObject.getName() + "\r\n" +
                                    "Выберите материал:");
            List<Material> materialList = (List<Material>) materialRepository.findAll();
            HashMap<String, String> btnsTmp = new HashMap<>();
            for (int i = 0; i < materialList.size(); ++i) {
                btnsTmp.put(materialList.get(i).getMaterialId().toString(), materialList.get(i).getName());
            }
            editMessageText.setReplyMarkup(buttonService.createVerticalMenu(btnsTmp));
            stateService.setState(user, State.CONSUMP_WAIT_MATERIAL);
        }
        return editMessageText;
    }
    private BotApiMethod consumpWaitMaterialLogic(User user, Update update) {
        EditMessageText editMessageText = new EditMessageText();
        if (update.hasCallbackQuery()) {
            Material material = materialRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).get();
            materialTmp.get(user).setMaterial(material);
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId((int) messageId);
            stateService.setState(user, State.CONSUMP_WAIT_MATERIAL_CNT);
            editMessageText.setText("Выбран объект: " + materialTmp.get(user).getBuildObject().getName() + "\r\n " +
                                    "Выбран материал: " + material.getName()+ "\r\n " +
                                    "Введите количество:\n");
        }
        return editMessageText;
    }
    private BotApiMethod consumpWaitMaterialCntLogic(User user, Update update) {
        materialTmp.get(user).setCnt(Double.parseDouble(update.getMessage().getText()));
        stateService.setState(user, State.FREE);
        String chatId = String.valueOf(update.getMessage().getChatId());
        materialConsumpRepository.save(materialTmp.get(user));

        return new SendMessage(chatId, "Новая трата успешно сохранена по объекту: \r\n"
                + materialTmp.get(user).getBuildObject().getName() + " \r\n"
                + materialTmp.get(user).getMaterial().getName() + " \r\n"
                + ", количество:" +  materialTmp.get(user).getCnt());
    }

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public String getDescription() {
        return "Добавить расход материала";
    }
}
