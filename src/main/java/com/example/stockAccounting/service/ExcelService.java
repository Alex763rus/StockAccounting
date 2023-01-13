package com.example.stockAccounting.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ExcelService {

    public InputFile createExcelDocument(String sheetName, List<List<String>> excelData){
        File tmpFile;
        try {
            tmpFile = Files.createTempFile("exel", ".xls").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Workbook book = new HSSFWorkbook();
        Sheet sheet = book.createSheet(sheetName);

        for (int y = 0; y < excelData.size(); y++) {
            Row row = sheet.createRow(y);
            for (int x = 0; x < excelData.get(y).size(); x++) {
                row.createCell(x).setCellValue(excelData.get(y).get(x));
            }
        }
        for (int x = 0; x < excelData.get(0).size(); x++) {
            sheet.autoSizeColumn(x);
        }

        try {
            book.write(new FileOutputStream(tmpFile));
            book.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new InputFile(tmpFile);
    }
}
