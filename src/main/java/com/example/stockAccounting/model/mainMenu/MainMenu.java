package com.example.stockAccounting.model.mainMenu;

import com.example.stockAccounting.service.ButtonService;
import com.example.stockAccounting.service.ExcelService;
import com.example.stockAccounting.service.StateService;
import com.example.stockAccounting.service.database.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class MainMenu implements MainMenuActivity {

    @Autowired
    protected UserService userService;

    @Autowired
    protected StateService stateService;

    @Autowired
    protected ButtonService buttonService;

    @Autowired
    protected ExcelService excelService;

}
