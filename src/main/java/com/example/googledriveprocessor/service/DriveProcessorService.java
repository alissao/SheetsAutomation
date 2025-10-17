package com.example.googledriveprocessor.service;

import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriveProcessorService {

    private final DocumentProcessingService documentProcessingService;
    private final GoogleSheetsService sheetsService;

    public String processFolder(String folderId) {
        try {
            log.info("Starting to process folder: {}", folderId);
            
            // Process all files in the folder
            List<String> processResults = documentProcessingService.processFolder(folderId);
            
            // Convert results to format suitable for Google Sheets
            List<List<Object>> rows = new ArrayList<>();
            for (String result : processResults) {
                // Simple parsing of the result string - adjust based on your actual DocumentProcessingService output
                String[] parts = result.split(":", 2);
                if (parts.length == 2) {
                    String fileName = parts[0].replace("Processed ", "").trim();
                    String content = parts[1].trim();
                    rows.add(List.of(fileName, content));
                }
            }
            
            // Store results in Google Sheets
            if (!rows.isEmpty()) {
                sheetsService.appendData(rows, "Processed Documents");
                return String.format("Successfully processed %d documents from folder: %s", 
                        rows.size(), folderId);
            } else {
                return "No supported documents found to process in folder: " + folderId;
            }
            
        } catch (Exception e) {
            log.error("Error processing folder: {}", folderId, e);
            return "Error processing folder " + folderId + ": " + e.getMessage();
        }
    }
}
