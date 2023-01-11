package com.example.stockAccounting.service;

import com.example.stockAccounting.config.BotConfig;
import com.example.stockAccounting.model.User;
import com.example.stockAccounting.model.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    final long ADMIN_CHAT_ID = 799008767;
    final private BotConfig botConfig;

    static final String COMMAND_START = "/start";
    static final String COMMAND_MY_DATA = "/mydata";
//    static final String COMMAND_DELETE = "/delete";
    static final String COMMAND_HELP = "/help";
    static final String COMMAND_SETTINGS = "/settings";
    static final String COMMAND_TEST_FEATURE = "/deLete";

    static final String COMMAND_REGISTER = "/register";

    static final String TEXT_HELP = "this bot is created bla bla bla.....";

    @Autowired
    private UserRepository userRepository;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand(COMMAND_START, "get a welcome message"));
//        listofCommands.add(new BotCommand(COMMAND_MY_DATA, "get your data store"));
//        listofCommands.add(new BotCommand(COMMAND_DELETE, "delete my data"));
//        listofCommands.add(new BotCommand(COMMAND_HELP, "info how to use this bot"));
//        listofCommands.add(new BotCommand(COMMAND_SETTINGS, "set your preferences"));
        listofCommands.add(new BotCommand(COMMAND_TEST_FEATURE, "get tralli wali"));
//        listofCommands.add(new BotCommand(COMMAND_REGISTER, "wwwww"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }

        log.info("Server was starded. Version: " + botConfig.getBotVersion());
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case COMMAND_START:
                    registeredUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case COMMAND_MY_DATA:
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case COMMAND_HELP:
                    sendMessage(chatId, TEXT_HELP);
                    break;
                case COMMAND_TEST_FEATURE:
                    testFeature(chatId, update.getMessage());
                    break;
//                case COMMAND_REGISTER:
//                    testFeature(chatId, update.getMessage());
//                    break;
                default:
                    sendMessage(chatId, "..!..");

            }
        }
    }

    private void register(long chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Вы действительно хотите зарегистрироваться?");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var yes_button = new InlineKeyboardButton();
        yes_button.setText("Yes");
        yes_button.setCallbackData("YES_BUTTON");

        var no_button = new InlineKeyboardButton();
        yes_button.setText("No");
        yes_button.setCallbackData("NO_BUTTON");

        rowInline.add(yes_button);
        rowInline.add(no_button);

        rowsInline.add(rowInline);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
    private void registeredUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            ArrayList<User> userList = new ArrayList<>();
            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    private void testFeature(long chatId, Message message) {
//        sendMessage(ADMIN_CHAT_ID, "User " + message.getChat().getFirstName() + " send message from bot:" + message.getText());
//        sendMessage(message.getChatId(), "OK, wait.... ..!..");
    }

    private void startCommandReceived(long chatId, String userName) {
        // String answer = "Hello, " + userName + "!";
        String answer = EmojiParser.parseToUnicode("Hello, " + userName + "!" + " :blush:");
        log.info("Replied to user " + userName);
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
