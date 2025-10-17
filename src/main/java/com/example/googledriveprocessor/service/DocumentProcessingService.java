package com.example.googledriveprocessor.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private static final List<String> SUPPORTED_MIME_TYPES = List.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain",
            "image/jpeg",
            "image/png",
            // Google Docs native types
            "application/vnd.google-apps.document",
            "application/vnd.google-apps.spreadsheet",
            "application/vnd.google-apps.presentation"
    );

    private final Drive driveService;

    public List<String> processFolder(String folderId) {
        try {
            List<String> results = new ArrayList<>();
            FileList result = driveService.files().list()
                    .setQ("'" + folderId + "' in parents and trashed = false")
                    .setFields("nextPageToken, files(id, name, mimeType, webViewLink)")
                    .execute();

            for (File file : result.getFiles()) {
                String mimeType = file.getMimeType();
                if (isSupportedFileType(mimeType)) {
                    String content = extractTextFromFile(file.getId(), mimeType);
                    results.add(String.format("Processed %s: %s", file.getName(), content.substring(0, Math.min(100, content.length()))));
                }
            }
            return results;
        } catch (IOException e) {
            log.error("Error processing folder: {}", folderId, e);
            return Collections.singletonList("Error processing folder: " + e.getMessage());
        }
    }

    private boolean isSupportedFileType(String mimeType) {
        return SUPPORTED_MIME_TYPES.stream().anyMatch(mimeType::startsWith);
    }

    private String extractTextFromFile(String fileId, String mimeType) throws IOException {
        try (InputStream inputStream = openInputStream(fileId, mimeType)) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            metadata.set(Metadata.CONTENT_TYPE, mimeType);
            ParseContext context = new ParseContext();
            Parser parser = new AutoDetectParser();

            parser.parse(inputStream, handler, metadata, context);
            return handler.toString();

        } catch (SAXException | TikaException e) {
            log.error("Error extracting text from file: {}", fileId, e);
            return "[Error extracting content: " + e.getMessage() + "]";
        }
    }

    private InputStream openInputStream(String fileId, String mimeType) throws IOException {
        // Handle Google Docs native files by exporting to a suitable format
        switch (mimeType) {
            case "application/vnd.google-apps.document":
                return driveService.files().export(fileId, "text/plain").executeMediaAsInputStream();
            case "application/vnd.google-apps.spreadsheet":
                return driveService.files().export(fileId, "text/csv").executeMediaAsInputStream();
            case "application/vnd.google-apps.presentation":
                return driveService.files().export(fileId, "text/plain").executeMediaAsInputStream();
            default:
                return driveService.files().get(fileId).executeMediaAsInputStream();
        }
    }
}
