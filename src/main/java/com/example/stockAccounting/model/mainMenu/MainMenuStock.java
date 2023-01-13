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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.stockAccounting.model.mainMenu.MainMenuStatus.*;

@Component
@Slf4j
public class MainMenuStock implements MainMenuActivity {

    final String MENU_NAME = "/stock";

    @Autowired
    private StockRepository stockRepository;

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
        return "База";
    }

    @Autowired
    ExcelService excelService;
    private HashMap<String, String> btnsMaterial = new HashMap<>();
    private Material materialTmp;

    @Override
    public PartialBotApiMethod menuRun(Update update) {
        PartialBotApiMethod answer = null;
        switch (mainMenuStatus) {
            case FREE:
                answer = freeLogic(update);
                break;
            case STOCK_MAIN:
                answer = stockMainLogic(update);
                break;
            case STOCK_WAIT_MATERIAL:
                answer = stockWaitMaterialLogic(update);
                break;
            case STOCK_WAIT_MATERIAL_CNT:
                answer = stockWaitMaterialCntLogic(update);
                break;
        }
        return answer;
    }

    private BotApiMethod freeLogic(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText("Выберите режим работы с Базой:");

        HashMap<String, String> btns = new HashMap<>();
        btns.put((String.valueOf(STOCK_SHOW)), "Показать материал");
        btns.put(String.valueOf(STOCK_DOWNLOAD_ALL), "Выгрузить материалы в Excel");
        btns.put(String.valueOf(STOCK_ADD), "Добавить материал");
        sendMessage.setReplyMarkup(buttonService.createVerticalMenu(btns));

        mainMenuStatus = MainMenuStatus.STOCK_MAIN;
        return sendMessage;
    }

    private PartialBotApiMethod stockMainLogic(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals(String.valueOf(STOCK_DOWNLOAD_ALL))) {
                List<Stock> stockList = (List<Stock>) stockRepository.findAll();
                List<List<String>> excelData = new ArrayList<>();
                excelData.add(Arrays.asList("№","Материал:","Количество"));
                for (int i = 0; i < stockList.size(); ++i) {
                    excelData.add(Arrays.asList(String.valueOf(i + 1)
                                    , stockList.get(i).getMaterial().getName()
                                    , String.valueOf(stockList.get(i).getCnt()).replace(".", ",")));
                }
                SendDocument sendDocument = new SendDocument();
                sendDocument.setDocument(excelService.createExcelDocument("Склад", excelData));
                sendDocument.setChatId(String.valueOf(chatId));
                mainMenuStatus = MainMenuStatus.FREE;
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
                mainMenuStatus = MainMenuStatus.FREE;
            } else if (callBackData.equals(String.valueOf(STOCK_ADD))) {
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setMessageId((int) messageId);
                editMessageText.setText("Режим оформления поставки на склад.\n" +
                        "выберите материал:");

                List<Material> materialList = (List<Material>) materialRepository.findAll();
                btnsMaterial.clear();
                for (int i = 0; i < materialList.size(); ++i) {
                    btnsMaterial.put(materialList.get(i).getMaterialId().toString(), materialList.get(i).getName());
                }
                editMessageText.setReplyMarkup(buttonService.createVerticalMenu(btnsMaterial));
                mainMenuStatus = MainMenuStatus.STOCK_WAIT_MATERIAL;
            }
        }
        return editMessageText;
    }
    private BotApiMethod stockWaitMaterialLogic(Update update) {
        EditMessageText editMessageText = new EditMessageText();
        if (update.hasCallbackQuery()) {
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId((int) messageId);

            materialTmp = materialRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).orElse(null);
            mainMenuStatus = MainMenuStatus.STOCK_WAIT_MATERIAL_CNT;
            editMessageText.setText("Введите количество:\n");
        }
        return editMessageText;
    }
    private BotApiMethod stockWaitMaterialCntLogic(Update update) {
        Stock stock = new Stock();
        stock.setMaterial(materialTmp);
        stock.setCnt(Double.parseDouble(update.getMessage().getText()));
        mainMenuStatus = MainMenuStatus.FREE;
        String chatId = String.valueOf(update.getMessage().getChatId());
        stockRepository.save(stock);
        return new SendMessage(chatId, "Новая поставка успешно сохранена: " + stock.getMaterial().getName() + " " + stock.getCnt());
    }
    public MainMenuStatus getStatus() {
        return mainMenuStatus;
    }

}
