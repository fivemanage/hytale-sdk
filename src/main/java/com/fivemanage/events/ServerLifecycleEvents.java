package com.fivemanage.events;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;

import java.util.HashMap;
import java.util.Map;

public class ServerLifecycleEvents {
    private static long startTime;
    private static String dataset = "default";

    public static void setDataset(String ds) {
        dataset = ds;
    }

    public static void onServerStarted() {
        startTime = System.currentTimeMillis();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Server Started");
        metadata.put("timestamp", startTime);

        FivemanageLogger.info(dataset, "server.started", metadata);
    }

    public static void onShutdown(ShutdownEvent event) {
        long uptimeMs = System.currentTimeMillis() - startTime;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Server Shutdown");
        metadata.put("uptimeMs", uptimeMs);
        metadata.put("uptimeSeconds", uptimeMs / 1000);

        FivemanageLogger.info(dataset, "server.shutdown", metadata);
    }
}
