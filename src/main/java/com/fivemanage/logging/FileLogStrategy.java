package com.fivemanage.logging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FileLogStrategy implements LogStrategy {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Path logDirectory;

    @Override
    public void initialize(Map<String, String> config) {
        String logPath = config.getOrDefault("logPath", "logs");
        this.logDirectory = Paths.get(logPath);

        try {
            Files.createDirectories(logDirectory);
            System.out.println("[FileLogStrategy] Log directory created/verified at: " + logDirectory.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[FileLogStrategy] Failed to create log directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getProviderName() {
        return "File";
    }

    static class LogEntry {
        String timestamp;
        String level;
        String dataset;
        String resource;
        String message;
        Map<String, Object> metadata;

        LogEntry(String timestamp, String level, String dataset, String resource, String message, Map<String, Object> metadata) {
            this.timestamp = timestamp;
            this.level = level;
            this.dataset = dataset;
            this.resource = resource;
            this.message = message;
            this.metadata = metadata;
        }
    }

    @Override
    public void sendLog(String dataset, String level, String message, Map<String, Object> metadata, String resource) {
        if (logDirectory == null) {
            System.err.println("[FileLogStrategy] Log directory not initialized!");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
                String dateStr = LocalDateTime.now().format(DATE_FORMATTER);

                String fileName = String.format("%s_%s.jsonl", dateStr, dataset);
                Path logFile = logDirectory.resolve(fileName);

                LogEntry logEntry = new LogEntry(timestamp, level, dataset, resource, message, metadata);
                String jsonLine = gson.toJson(logEntry);

                try (BufferedWriter writer = Files.newBufferedWriter(logFile,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)) {
                    writer.write(jsonLine);
                    writer.newLine();
                }

                System.out.println("[FileLogStrategy] Log written to: " + logFile.getFileName());
            } catch (IOException e) {
                System.err.println("[FileLogStrategy] Failed to write log to file: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void flush() {
        // uhh what am I doing here?
    }

    @Override
    public void shutdown() {
        // No resources to clean up
        System.out.println("[FileLogStrategy] Shutdown complete");
    }
}
