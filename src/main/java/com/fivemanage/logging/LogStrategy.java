package com.fivemanage.logging;

import java.util.Map;

public interface LogStrategy {
    void sendLog(String dataset, String level, String message, Map<String, Object> metadata, String resource);
    void initialize(Map<String, String> config);
    String getProviderName();

    /**
     * flush buffered logs after x amount of ms
     */
    void flush();

    /**
     * just to make sure capture all logs on crashes or shutdown
     */
    void shutdown();
}
