package com.example.stockAccounting.model.mainMenu;

import com.example.stockAccounting.enums.State;
import com.example.stockAccounting.model.jpa.BuildObject;
import com.example.stockAccounting.model.jpa.BuildObjectRepository;
import com.example.stockAccounting.model.jpa.User;
import com.example.stockAccounting.service.ExcelService;
import com.example.stockAccounting.service.StateService;
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
public class MainMenuBuildObject extends MainMenu {

    final String MENU_NAME = "/build_object";

    @Autowired
    private BuildObjectRepository buildObjectRepository;

    @Override
    public PartialBotApiMethod menuRun(User user, Update update) {
        PartialBotApiMethod answer = null;
        State state = stateService.getState(user);
        switch (state) {
            case FREE:
                answer = freeLogic(user, update);
                break;
            case BUILD_OBJECT_MAIN:
                answer = buildObjectLogic(user, update);
                break;
            case BUILD_OBJECT_WAIT_OBJECT_NAME:
                answer = waitObjectNameLogic(user, update);
                break;
        }
        return answer;
    }

    private BotApiMethod freeLogic(User user, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText("Выберите режим работы с Объектами:");

        HashMap<String, String> btns = new HashMap<>();
        btns.put(String.valueOf(State.BUILD_OBJECT_SHOW_ALL), "Показать все объекты");
        btns.put(String.valueOf(State.BUILD_OBJECT_DOWNLOAD_ALL), "Выгрузить все объекты в Excel");
        btns.put(String.valueOf(State.BUILD_OBJECT_ADD), "Добавить объект");
        sendMessage.setReplyMarkup(buttonService.createVerticalMenu(btns));

        stateService.setState(user, State.BUILD_OBJECT_MAIN);
        return sendMessage;
    }

    private PartialBotApiMethod buildObjectLogic(User user, Update update) {
        EditMessageText editMessageText = new EditMessageText();
        if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals(String.valueOf(State.BUILD_OBJECT_DOWNLOAD_ALL))) {
                List<BuildObject> buildObjectList = (List<BuildObject>) buildObjectRepository.findAll();
                List<List<String>> excelData = new ArrayList<>();
                excelData.add(Arrays.asList("№","Объект:"));
                for (int i = 0; i < buildObjectList.size(); ++i) {
                    excelData.add(Arrays.asList(String.valueOf(i + 1), buildObjectList.get(i).getName()));
                }
                SendDocument sendDocument = new SendDocument();
                sendDocument.setDocument(excelService.createExcelDocument("Объекты", excelData));
                sendDocument.setChatId(String.valueOf(chatId));
                stateService.setState(user, State.FREE);
                return sendDocument;
            } else if (callBackData.equals(String.valueOf(State.BUILD_OBJECT_SHOW_ALL))) {
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId((int) messageId);
                List<BuildObject> buildObjectList = (List<BuildObject>) buildObjectRepository.findAll();
                StringBuilder buildObjects = new StringBuilder("Объекты:\n");
                for (int i = 0; i < buildObjectList.size(); ++i) {
                    buildObjects.append(i + 1).append(") ").append(buildObjectList.get(i).getName()).append("\r\n");
                }
                editMessageText.setText(buildObjects.toString());
                stateService.setState(user, State.FREE);
            } else if (callBackData.equals(String.valueOf(State.BUILD_OBJECT_ADD))) {
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId((int) messageId);
                editMessageText.setText("Режим добавления нового объекта.\n" +
                        "Введите название нового объекта:");
                stateService.setState(user, State.BUILD_OBJECT_WAIT_OBJECT_NAME);
            }
        }
        return editMessageText;
    }

    private BotApiMethod waitObjectNameLogic(User user, Update update) {
        BuildObject buildObject = new BuildObject();
        buildObject.setName(update.getMessage().getText());
        stateService.setState(user, State.FREE);
        String chatId = String.valueOf(update.getMessage().getChatId());
        buildObjectRepository.save(buildObject);
        return new SendMessage(chatId, "Новый объект успешно сохранен.");
    }
    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public String getDescription() {
        return "Объекты";
    }
}
