package com.fivemanage.logging;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.*;

public class FivemanageLogStrategy implements LogStrategy {
    private static final String API_URL = "https://api.fivemanage.com/api/logs/batch";
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private String apiKey;
    private boolean enableBatching;
    private int bufferSize;
    private int flushIntervalMs;

    private final Map<String, List<LogData>> buffers = new ConcurrentHashMap<>();
    private final Object bufferLock = new Object();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> flushTask;

    @Override
    public void initialize(Map<String, String> config) {
        this.apiKey = config.get("apiKey");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("[FivemanageLogStrategy] API Key not provided in config!");
        }

        this.enableBatching = Boolean.parseBoolean(config.getOrDefault("enableBatching", "true"));
        this.bufferSize = Integer.parseInt(config.getOrDefault("bufferSize", "10"));
        this.flushIntervalMs = Integer.parseInt(config.getOrDefault("flushIntervalMs", "5000"));

        if (enableBatching) {
            scheduler = Executors.newScheduledThreadPool(1);
            flushTask = scheduler.scheduleAtFixedRate(
                this::flush,
                flushIntervalMs,
                flushIntervalMs,
                TimeUnit.MILLISECONDS
            );
            System.out.println("[FivemanageLogStrategy] Batching enabled - buffer size: " + bufferSize + ", flush interval: " + flushIntervalMs + "ms");
        }
    }

    @Override
    public String getProviderName() {
        return "Fivemanage";
    }

    static class LogData {
        String level;
        String message;
        String resource;
        Map<String, Object> metadata;

        LogData(String level, String message, Map<String, Object> metadata, String resource) {
            this.level = level;
            this.message = message;
            this.resource = resource;
            this.metadata = metadata;
        }
    }

    @Override
    public void sendLog(String dataset, String level, String message, Map<String, Object> metadata, String resource) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("[FivemanageLogStrategy] API Key not initialized!");
            return;
        }

        LogData logData = new LogData(level, message, metadata, resource);

        if (!enableBatching) {
            sendBatch(dataset, Collections.singletonList(logData));
            return;
        }

        synchronized (bufferLock) {
            buffers.computeIfAbsent(dataset, k -> new ArrayList<>()).add(logData);

            if (buffers.get(dataset).size() >= bufferSize) {
                flushDataset(dataset);
            }
        }
    }

    @Override
    public void flush() {
        synchronized (bufferLock) {
            for (String dataset : new ArrayList<>(buffers.keySet())) {
                flushDataset(dataset);
            }
        }
    }

    @Override
    public void shutdown() {
        System.out.println("[FivemanageLogStrategy] Shutting down...");
        flush();

        if (scheduler != null) {
            if (flushTask != null) {
                flushTask.cancel(false);
            }
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void flushDataset(String dataset) {
        List<LogData> logsToSend = buffers.remove(dataset);
        if (logsToSend == null || logsToSend.isEmpty()) {
            return;
        }

        sendBatch(dataset, logsToSend);
    }

    private void sendBatch(String dataset, List<LogData> logs) {
        CompletableFuture.runAsync(() -> {
            try {
                String jsonBody = gson.toJson(logs);

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", apiKey)
                    .header("X-Fivemanage-Dataset", dataset)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("[FivemanageLogStrategy] Sent batch of " + logs.size() + " logs for dataset: " + dataset);
                } else {
                    System.err.println("[FivemanageLogStrategy] API error: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                System.err.println("[FivemanageLogStrategy] Failed to send batch: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
