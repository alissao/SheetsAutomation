package com.example.googledriveprocessor.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Configuration
public class GoogleDriveConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(
            DriveScopes.DRIVE_READONLY,
            SheetsScopes.SPREADSHEETS
    );

    @Bean
    public com.google.api.services.drive.Drive getDriveService() throws IOException {
        final NetHttpTransport httpTransport = new NetHttpTransport();
        GoogleCredentials credentials = loadCredentials();
        return new com.google.api.services.drive.Drive.Builder(
                httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName("Google Drive Document Processor")
                .build();
    }

    @Bean
    public com.google.api.services.sheets.v4.Sheets getSheetsService() throws IOException {
        final NetHttpTransport httpTransport = new NetHttpTransport();
        GoogleCredentials credentials = loadCredentials();
        return new com.google.api.services.sheets.v4.Sheets.Builder(
                httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName("Google Sheets Document Processor")
                .build();
    }

    private GoogleCredentials loadCredentials() throws IOException {
        GoogleCredentials credentials;
        String credPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credPath != null && !credPath.isBlank()) {
            try (FileInputStream in = new FileInputStream(credPath)) {
                credentials = GoogleCredentials.fromStream(in);
            }
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
        }
        if (credentials.createScopedRequired()) {
            credentials = credentials.createScoped(SCOPES);
        } else {
            credentials = credentials.createScoped(SCOPES);
        }
        return credentials;
    }
}
