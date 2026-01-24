package com.fivemanage.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.HashMap;
import java.util.Map;

public class LogProviderConfig {
    public static final BuilderCodec<LogProviderConfig> CODEC =
        BuilderCodec.builder(LogProviderConfig.class, LogProviderConfig::new)
            .append(new KeyedCodec<String>("Provider", Codec.STRING),
                    (config, value) -> config.provider = value,
                    (config) -> config.provider)
            .add()
            .append(new KeyedCodec<String>("ApiKey", Codec.STRING),
                    (config, value) -> config.apiKey = value,
                    (config) -> config.apiKey)
            .add()
            .append(new KeyedCodec<String>("Endpoint", Codec.STRING),
                    (config, value) -> config.endpoint = value,
                    (config) -> config.endpoint)
            .add()
            .append(new KeyedCodec<String>("Username", Codec.STRING),
                    (config, value) -> config.username = value,
                    (config) -> config.username)
            .add()
            .append(new KeyedCodec<String>("Password", Codec.STRING),
                    (config, value) -> config.password = value,
                    (config) -> config.password)
            .add()
            .append(new KeyedCodec<Boolean>("WriteToDisk", Codec.BOOLEAN),
                    (config, value) -> config.writeToDisk = value,
                    (config) -> config.writeToDisk)
            .add()
            .append(new KeyedCodec<Boolean>("EnableBatching", Codec.BOOLEAN),
                    (config, value) -> config.enableBatching = value,
                    (config) -> config.enableBatching)
            .add()
            .append(new KeyedCodec<Integer>("BufferSize", Codec.INTEGER),
                    (config, value) -> config.bufferSize = value,
                    (config) -> config.bufferSize)
            .add()
            .append(new KeyedCodec<Integer>("FlushIntervalMs", Codec.INTEGER),
                    (config, value) -> config.flushIntervalMs = value,
                    (config) -> config.flushIntervalMs)
            .add()
            .build();

    private String provider = "fivemanage"; // Default to fivemanage for backwards compatibility
    private String apiKey = "";
    private String endpoint = "";
    private String username = "";
    private String password = "";
    private boolean writeToDisk = false;
    private boolean enableBatching = true;
    private int bufferSize = 10;
    private int flushIntervalMs = 5000;

    public LogProviderConfig() {
    }

    public String getProvider() {
        return provider;
    }

    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        if (!apiKey.isEmpty()) config.put("apiKey", apiKey);
        if (!endpoint.isEmpty()) config.put("endpoint", endpoint);
        if (!username.isEmpty()) config.put("username", username);
        if (!password.isEmpty()) config.put("password", password);
        config.put("writeToDisk", String.valueOf(writeToDisk));
        config.put("enableBatching", String.valueOf(enableBatching));
        config.put("bufferSize", String.valueOf(bufferSize));
        config.put("flushIntervalMs", String.valueOf(flushIntervalMs));
        return config;
    }

    public boolean isWriteToDisk() {
        return writeToDisk;
    }

    public boolean isEnableBatching() {
        return enableBatching;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getFlushIntervalMs() {
        return flushIntervalMs;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setWriteToDisk(boolean writeToDisk) {
        this.writeToDisk = writeToDisk;
    }

    public void setEnableBatching(boolean enableBatching) {
        this.enableBatching = enableBatching;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setFlushIntervalMs(int flushIntervalMs) {
        this.flushIntervalMs = flushIntervalMs;
    }
}
