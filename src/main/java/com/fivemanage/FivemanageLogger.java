package com.fivemanage;

import com.fivemanage.logging.FileLogStrategy;
import com.fivemanage.logging.FivemanageLogStrategy;
import com.fivemanage.logging.GrafanaLokiLogStrategy;
import com.fivemanage.logging.LogStrategy;

import java.util.HashMap;
import java.util.Map;

public class FivemanageLogger {
    private static LogStrategy logStrategy;
    private static LogStrategy fileLogStrategy;

    /**
     * Initialize logger with strategy
     * @param strategy The logging strategy to use
     * @param config Configuration map for the strategy
     */
    public static void initialize(LogStrategy strategy, Map<String, String> config) {
        logStrategy = strategy;
        logStrategy.initialize(config);
        System.out.println("[FivemanageLogger] Initialized with provider: " + strategy.getProviderName());
    }

    /**
     * Initialize with the provider name
     * @param provider Provider name: "fivemanage", "grafana-loki", etc.
     * @param config Configuration map for the strategy
     */
    public static void initialize(String provider, Map<String, String> config) {
        LogStrategy strategy = switch (provider.toLowerCase()) {
            case "fivemanage" -> new FivemanageLogStrategy();
            case "grafana-loki", "loki" -> new GrafanaLokiLogStrategy();
            default -> {
                System.err.println("[FivemanageLogger] Unknown provider: " + provider + ". Defaulting to Fivemanage.");
                yield new FivemanageLogStrategy();
            }
        };
        initialize(strategy, config);

        String writeToDisk = config.get("writeToDisk");
        if ("true".equalsIgnoreCase(writeToDisk)) {
            fileLogStrategy = new FileLogStrategy();
            fileLogStrategy.initialize(config);
            System.out.println("[FivemanageLogger] File logging enabled");
        }
    }

    public static void log(String dataset, String level, String message, Map<String, Object> metadata) {
        if (logStrategy == null) {
            System.err.println("[FivemanageLogger] Logger not initialized! Call initialize() first.");
            return;
        }

        String caller = StackWalker.getInstance()
            .walk(frames -> frames
                    // skip the two first, which SHOULD be two internal methods,
                    // havent tested this through another resource yet tho
                .skip(2)
                .findFirst()
                .map(StackWalker.StackFrame::getClassName)
                .orElse("Unknown"));

        if (metadata == null) {
            metadata = new HashMap<>();
        }
        Map<String, Object> finalMetadata = new HashMap<>(metadata);

        logStrategy.sendLog(dataset, level, message, finalMetadata, caller);

        if (fileLogStrategy != null) {
            fileLogStrategy.sendLog(dataset, level, message, finalMetadata, caller);
        }
    }

    public static void info(String dataset, String message, Map<String, Object> metadata) {
        log(dataset, "info", message, metadata);
    }

    public static void warn(String dataset, String message, Map<String, Object> metadata) {
        log(dataset, "warn", message, metadata);
    }

    public static void error(String dataset, String message, Map<String, Object> metadata) {
        log(dataset, "error", message, metadata);
    }

    public static void debug(String dataset, String message, Map<String, Object> metadata) {
        log(dataset, "debug", message, metadata);
    }

    /**
     * shutdown logger, flush and cleanup
     */
    public static void shutdown() {
        System.out.println("[FivemanageLogger] Shutting down logger...");
        if (logStrategy != null) {
            logStrategy.shutdown();
        }
        if (fileLogStrategy != null) {
            fileLogStrategy.shutdown();
        }
        System.out.println("[FivemanageLogger] Logger shutdown complete");
    }
}
