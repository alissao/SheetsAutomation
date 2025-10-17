package com.example.googledriveprocessor.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    private final Sheets sheetsService;

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    public void appendData(List<List<Object>> data, String sheetName) {
        try {
            // Create sheet if it doesn't exist
            createSheetIfNotExists(sheetName);

            // Prepare the data
            List<ValueRange> dataToAppend = new ArrayList<>();
            ValueRange body = new ValueRange()
                    .setValues(Collections.singletonList(
                            Arrays.asList("File Name", "Content Snippet", "Processed At")
                    ));
            
            // Add header if sheet is empty
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, sheetName + "!A1:C1")
                    .execute();
            
            if (response.getValues() == null || response.getValues().isEmpty()) {
                sheetsService.spreadsheets().values()
                        .update(spreadsheetId, sheetName + "!A1", body)
                        .setValueInputOption("RAW")
                        .execute();
            }

            // Prepare data rows
            List<List<Object>> rows = data.stream()
                    .map(row -> Arrays.asList(
                            row.get(0), // file name
                            row.get(1), // content
                            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(new java.util.Date())
                    ))
                    .collect(Collectors.toList());

            // Append data
            ValueRange appendBody = new ValueRange()
                    .setValues(rows);

            AppendValuesResponse result = sheetsService.spreadsheets().values()
                    .append(spreadsheetId, sheetName + "!A2", appendBody)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(true)
                    .execute();

            log.info("Appended {} rows to sheet: {}", 
                    result.getUpdates().getUpdatedRows(), sheetName);

        } catch (IOException e) {
            log.error("Error appending data to Google Sheets", e);
            throw new RuntimeException("Failed to update Google Sheet", e);
        }
    }

    private void createSheetIfNotExists(String sheetName) throws IOException {
        try {
            // Try to get the sheet to check if it exists
            sheetsService.spreadsheets().values()
                    .get(spreadsheetId, sheetName + "!A1")
                    .execute();
        } catch (Exception e) {
            // If sheet doesn't exist, create it
            AddSheetRequest addSheetRequest = new AddSheetRequest()
                    .setProperties(new SheetProperties().setTitle(sheetName));
            
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                    .setRequests(Collections.singletonList(
                            new Request().setAddSheet(addSheetRequest)
                    ));
            
            sheetsService.spreadsheets()
                    .batchUpdate(spreadsheetId, batchUpdateRequest)
                    .execute();
            
            log.info("Created new sheet: {}", sheetName);
        }
    }
}
