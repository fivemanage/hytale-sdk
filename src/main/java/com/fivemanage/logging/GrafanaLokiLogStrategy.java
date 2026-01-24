package com.fivemanage.logging;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.*;

public class GrafanaLokiLogStrategy implements LogStrategy {
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private String endpoint;
    private String username;
    private String password;
    private boolean enableBatching;
    private int bufferSize;
    private int flushIntervalMs;

    private final Map<String, StreamBuffer> buffers = new ConcurrentHashMap<>();
    private final Object bufferLock = new Object();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> flushTask;
    private int totalBufferedLogs = 0;

    @Override
    public void initialize(Map<String, String> config) {
        this.endpoint = config.get("endpoint");
        this.username = config.get("username");
        this.password = config.get("password");

        if (endpoint == null || endpoint.isEmpty()) {
            System.err.println("[GrafanaLokiLogStrategy] Endpoint not provided in config!");
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
            System.out.println("[GrafanaLokiLogStrategy] Batching enabled - buffer size: " + bufferSize + ", flush interval: " + flushIntervalMs + "ms");
        }
    }

    @Override
    public String getProviderName() {
        return "Grafana Loki";
    }

    static class LokiPushRequest {
        List<Stream> streams;

        LokiPushRequest(List<Stream> streams) {
            this.streams = streams;
        }
    }

    static class Stream {
        Map<String, String> stream;
        List<List<String>> values;

        Stream(Map<String, String> stream, List<List<String>> values) {
            this.stream = stream;
            this.values = values;
        }
    }

    static class StreamBuffer {
        Map<String, String> labels;
        List<List<String>> values;

        StreamBuffer(Map<String, String> labels) {
            this.labels = labels;
            this.values = new ArrayList<>();
        }

        void addValue(String timestamp, String logLine) {
            values.add(Arrays.asList(timestamp, logLine));
        }
    }

    @Override
    public void sendLog(String dataset, String level, String message, Map<String, Object> metadata, String resource) {
        if (endpoint == null || endpoint.isEmpty()) {
            System.err.println("[GrafanaLokiLogStrategy] Endpoint not initialized!");
            return;
        }

        Map<String, String> labels = new HashMap<>();
        labels.put("level", level);
        labels.put("dataset", dataset);
        labels.put("resource", resource);

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("message", message);
        if (metadata != null && !metadata.isEmpty()) {
            logEntry.put("metadata", metadata);
        }
        String logLine = gson.toJson(logEntry);
        String timestamp = String.valueOf(System.currentTimeMillis() * 1_000_000);

        if (!enableBatching) {
            sendBatch(Collections.singletonList(new Stream(labels, Collections.singletonList(Arrays.asList(timestamp, logLine)))));
            return;
        }

        synchronized (bufferLock) {
            String labelKey = getLabelKey(labels);
            StreamBuffer buffer = buffers.computeIfAbsent(labelKey, k -> new StreamBuffer(labels));
            buffer.addValue(timestamp, logLine);
            totalBufferedLogs++;

            if (totalBufferedLogs >= bufferSize) {
                flushAll();
            }
        }
    }

    @Override
    public void flush() {
        synchronized (bufferLock) {
            flushAll();
        }
    }

    @Override
    public void shutdown() {
        System.out.println("[GrafanaLokiLogStrategy] Shutting down...");
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

    private String getLabelKey(Map<String, String> labels) {
        return labels.get("level") + "|" + labels.get("dataset") + "|" + labels.get("resource");
    }

    private void flushAll() {
        if (buffers.isEmpty()) {
            return;
        }

        List<Stream> streams = new ArrayList<>();
        for (StreamBuffer buffer : buffers.values()) {
            streams.add(new Stream(buffer.labels, new ArrayList<>(buffer.values)));
        }

        buffers.clear();
        int logCount = totalBufferedLogs;
        totalBufferedLogs = 0;

        sendBatch(streams);
        System.out.println("[GrafanaLokiLogStrategy] Flushed batch of " + logCount + " logs in " + streams.size() + " stream(s)");
    }

    private void sendBatch(List<Stream> streams) {
        CompletableFuture.runAsync(() -> {
            try {
                LokiPushRequest pushRequest = new LokiPushRequest(streams);
                String jsonBody = gson.toJson(pushRequest);

                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint + "/loki/api/v1/push"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

                if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                    String auth = username + ":" + password;
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                    requestBuilder.header("Authorization", "Basic " + encodedAuth);
                }

                HttpRequest request = requestBuilder.build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("[GrafanaLokiLogStrategy] Batch sent successfully");
                } else {
                    System.err.println("[GrafanaLokiLogStrategy] API error: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                System.err.println("[GrafanaLokiLogStrategy] Failed to send batch: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
