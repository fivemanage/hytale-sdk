package com.fivemanage.events;

import com.fivemanage.FivemanageLogger;
import com.fivemanage.FivemanagePlugin;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ServerHeartbeat {
    private static final int DEFAULT_INTERVAL_MS = 30000;
    private static String dataset = "server-metrics";
    private static ScheduledExecutorService scheduler;
    private static ScheduledFuture<?> heartbeatTask;

    private ServerHeartbeat() {
    }

    public static synchronized void start(String dataset, int intervalMs) {
        stop();

        ServerHeartbeat.dataset = dataset;
        int safeIntervalMs = intervalMs;
        if (safeIntervalMs <= 0) {
            FivemanagePlugin.internalLogger.atWarning()
                .log("Invalid ServerHeartbeat.IntervalMs %s, using default %s ms", intervalMs, DEFAULT_INTERVAL_MS);
            safeIntervalMs = DEFAULT_INTERVAL_MS;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Fivemanage-ServerHeartbeat");
            thread.setDaemon(true);
            return thread;
        });
        heartbeatTask = scheduler.scheduleAtFixedRate(
            ServerHeartbeat::sendHeartbeatSafely,
            0,
            safeIntervalMs,
            TimeUnit.MILLISECONDS
        );
    }

    public static synchronized void stop() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
        }

        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private static void sendHeartbeatSafely() {
        try {
            sendHeartbeat();
        } catch (RuntimeException e) {
            FivemanagePlugin.internalLogger.atWarning().withCause(e).log("Failed to send server heartbeat");
        }
    }

    public static void sendHeartbeat() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Server Heartbeat");
        metadata.put("currentPlayers", Universe.get().getPlayerCount());
        metadata.put("maxPlayers", HytaleServer.get().getConfig().getMaxPlayers());
        metadata.put("timestamp", System.currentTimeMillis());

        FivemanageLogger.info(dataset, "server.player_count", metadata);
    }
}
