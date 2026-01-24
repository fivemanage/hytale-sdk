package com.fivemanage;

import com.fivemanage.config.LogProviderConfig;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.fivemanage.events.PlayerEvents;
import com.fivemanage.events.ecs.BreakBlockEvent;

import javax.annotation.Nonnull;

public class FivemanagePlugin extends JavaPlugin {
    private final Config<com.fivemanage.config.Config> config;
    public static final HytaleLogger internalLogger = HytaleLogger.forEnclosingClass();

    public FivemanagePlugin(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("config", com.fivemanage.config.Config.CODEC);
    }

    @Override
    protected void setup() {
        this.config.save();

        LogProviderConfig logProviderConfig = config.get().getLogProviderConfig();

        internalLogger.atInfo().log("Config loaded - Provider: " + logProviderConfig.getProvider());
        internalLogger.atInfo().log("Config map size: " + logProviderConfig.getConfig().size());
        internalLogger.atInfo().log("Config contents: " + logProviderConfig.getConfig());

        if (logProviderConfig == null || logProviderConfig.getConfig().isEmpty()) {
            internalLogger.atWarning().log("No logging configuration found, skipping plugin setup.");
            return;
        }

        FivemanageLogger.initialize(logProviderConfig.getProvider(), logProviderConfig.getConfig());
        internalLogger.atInfo().log("FivemanageLogger initialized with provider: " + logProviderConfig.getProvider());

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEvents::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, PlayerEvents::onPlayerConnect);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerEvents::onPlayerDisconnect);

        // kinda weird log with a lot of data
        // gotta figure out how to make this data valuable
        this.getEntityStoreRegistry().registerSystem(new BreakBlockEvent());
    }

    @Override
    protected void shutdown() {
        internalLogger.atInfo().log("Flushing logs before shutdown...");
        FivemanageLogger.shutdown();
    }
}