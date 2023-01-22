package com.example.stockAccounting.service.database;

import com.example.stockAccounting.model.jpa.User;
import com.example.stockAccounting.model.jpa.UserRepository;
import com.example.stockAccounting.service.StateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Timestamp;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StateService stateService;

    public User getUser(Message message){
        Long chatId = message.getChatId();
        User user = stateService.getUser(chatId);
        if(user == null){
            user = userRepository.findById(chatId).orElse(registeredUser(message));
        }
        return user;
    }

    private User registeredUser(Message message) {
        var chatId = message.getChatId();
        var chat = message.getChat();

        User user = new User();

        user.setChatId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUserName(chat.getUserName());
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        userRepository.save(user);
        log.info("user saved: " + user);
        return user;
    }
}
