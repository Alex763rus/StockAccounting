package com.example.stockAccounting.model.mainMenu;

import com.example.stockAccounting.enums.State;
import com.example.stockAccounting.model.jpa.*;
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

import java.util.*;

import static com.example.stockAccounting.enums.State.*;

@Component
@Slf4j
public class MainMenuStock extends MainMenu {

    final String MENU_NAME = "/stock";

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private MaterialRepository materialRepository;

    private Map<User, Material> materialTmp = new HashMap<>();

    @Override
    public PartialBotApiMethod menuRun(User user, Update update) {
        PartialBotApiMethod answer = null;
        State state = stateService.getState(user);
        switch (state) {
            case FREE:
                answer = freeLogic(user, update);
                break;
            case STOCK_MAIN:
                answer = stockMainLogic(user, update);
                break;
            case STOCK_WAIT_MATERIAL:
                answer = stockWaitMaterialLogic(user, update);
                break;
            case STOCK_WAIT_MATERIAL_CNT:
                answer = stockWaitMaterialCntLogic(user, update);
                break;
        }
        return answer;
    }

    private BotApiMethod freeLogic(User user, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText("Выберите режим работы с Базой:");

        HashMap<String, String> btns = new HashMap<>();
        btns.put((String.valueOf(STOCK_SHOW)), "Показать материал");
        btns.put(String.valueOf(STOCK_DOWNLOAD_ALL), "Выгрузить материалы в Excel");
        btns.put(String.valueOf(STOCK_ADD), "Добавить материал");
        sendMessage.setReplyMarkup(buttonService.createVerticalMenu(btns));

        stateService.setState(user, State.STOCK_MAIN);
        return sendMessage;
    }

    private PartialBotApiMethod stockMainLogic(User user, Update update) {
        EditMessageText editMessageText = new EditMessageText();
        if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals(String.valueOf(STOCK_DOWNLOAD_ALL))) {
                List<Stock> stockList = (List<Stock>) stockRepository.findAll();
                List<List<String>> excelData = new ArrayList<>();
                excelData.add(Arrays.asList("№", "Материал:", "Количество"));
                for (int i = 0; i < stockList.size(); ++i) {
                    excelData.add(Arrays.asList(String.valueOf(i + 1)
                            , stockList.get(i).getMaterial().getName()
                            , String.valueOf(stockList.get(i).getCnt()).replace(".", ",")));
                }
                SendDocument sendDocument = new SendDocument();
                sendDocument.setDocument(excelService.createExcelDocument("Склад", excelData));
                sendDocument.setChatId(String.valueOf(chatId));
                stateService.setState(user, State.FREE);
                return sendDocument;
            }
            if (callBackData.equals(String.valueOf(STOCK_SHOW))) {
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId((int) messageId);
                List<Stock> stockList = (List<Stock>) stockRepository.findAll();
                StringBuilder buildObjects = new StringBuilder("Материал на базе:\n");
                for (int i = 0; i < stockList.size(); ++i) {
                    buildObjects.append(i + 1).append(") ")
                            .append(stockList.get(i).getMaterial().getName()).append(" - ")
                            .append(stockList.get(i).getCnt()).append("\r\n");
                }
                editMessageText.setText(buildObjects.toString());
                stateService.setState(user, State.FREE);
            } else if (callBackData.equals(String.valueOf(STOCK_ADD))) {
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId((int) messageId);
                editMessageText.setText("Режим оформления поставки на склад.\n" +
                        "выберите материал:");

                List<Material> materialList = (List<Material>) materialRepository.findAll();
                HashMap<String, String> btnsMaterial = new HashMap<>();
                for (int i = 0; i < materialList.size(); ++i) {
                    btnsMaterial.put(materialList.get(i).getMaterialId().toString(), materialList.get(i).getName());
                }
                editMessageText.setReplyMarkup(buttonService.createVerticalMenu(btnsMaterial));
                stateService.setState(user, State.STOCK_WAIT_MATERIAL);
            }
        }
        return editMessageText;
    }

    private BotApiMethod stockWaitMaterialLogic(User user, Update update) {
        EditMessageText editMessageText = new EditMessageText();
        if (update.hasCallbackQuery()) {
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId((int) messageId);
            materialTmp.put(user, materialRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).get());
            stateService.setState(user, State.STOCK_WAIT_MATERIAL_CNT);
            editMessageText.setText("Введите количество:\n");
        }
        return editMessageText;
    }

    private BotApiMethod stockWaitMaterialCntLogic(User user, Update update) {
        Stock stock = new Stock();
        stock.setMaterial(materialTmp.get(user));
        stock.setCnt(Double.parseDouble(update.getMessage().getText()));
        stateService.setState(user, State.FREE);
        String chatId = String.valueOf(update.getMessage().getChatId());
        stockRepository.save(stock);
        materialTmp.remove(user);
        return new SendMessage(chatId, "Новая поставка успешно сохранена: " + stock.getMaterial().getName() + " " + stock.getCnt());
    }

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public String getDescription() {
        return "База";
    }

}
