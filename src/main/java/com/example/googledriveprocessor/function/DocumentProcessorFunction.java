package com.example.googledriveprocessor.function;

import com.example.googledriveprocessor.service.DriveProcessorService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class DocumentProcessorFunction {

    private final DriveProcessorService driveProcessorService;

    public DocumentProcessorFunction(DriveProcessorService driveProcessorService) {
        this.driveProcessorService = driveProcessorService;
    }

    @Bean
    public Function<String, String> processDocuments() {
        return folderId -> {
            try {
                return driveProcessorService.processFolder(folderId);
            } catch (Exception e) {
                return "Error processing documents: " + e.getMessage();
            }
        };
    }
}
