package com.example.stockAccounting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ButtonService {

    public InlineKeyboardMarkup createVerticalMenu(Map<String, String> menuDescription) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Map.Entry entry : menuDescription.entrySet()) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            var btn = new InlineKeyboardButton();
            btn.setText(entry.getValue().toString());
            btn.setCallbackData(entry.getKey().toString());
            rowInline.add(btn);
            rows.add(rowInline);
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
