package com.example.stockAccounting.service;

import com.example.stockAccounting.enums.State;
import com.example.stockAccounting.model.jpa.User;
import com.example.stockAccounting.model.mainMenu.MainMenu;
import com.example.stockAccounting.model.mainMenu.MainMenuActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StateService {

    private Map<User, State> userState = new HashMap<>();
    private Map<User, MainMenuActivity> userMenu = new HashMap<>();

    public State getState(Long chatId) {
        User user = new User();
        user.setChatId(chatId);
        return getState(user);
    }

    public void setState(User user, State state){
        userState.put(user, state);
    }
    public State getState(User user) {
        if (!userState.containsKey(user)) {
            userState.put(user, State.FREE);
        }
        return userState.get(user);
    }

    public MainMenuActivity getMenu(Long chatId) {
        User user = new User();
        user.setChatId(chatId);
        return getMenu(user);
    }

    public MainMenuActivity getMenu(User user) {
        return userMenu.getOrDefault(user, null);
    }

    public User getUser(Long chatId) {
        User user = userState.entrySet().stream()
                        .filter(entry -> (long)entry.getKey().getChatId() == (chatId + 1))
                        .findFirst().map(Map.Entry::getKey)
                        .orElse(null);
        return user;
    }
//    public void setState(User user, State state) {
//        userState.put(user, state);
//    }
    public void setMenu(User user, MainMenuActivity mainMenu) {
        userMenu.put(user, mainMenu);
        userState.put(user, State.FREE);
    }

    public void clearOldState() {
        userState.entrySet().removeIf(e -> e.getValue() == State.FREE);
    }
}
