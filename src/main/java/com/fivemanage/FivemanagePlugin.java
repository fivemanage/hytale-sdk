package com.fivemanage;

import com.fivemanage.config.EventsConfig;
import com.fivemanage.config.LogProviderConfig;
import com.fivemanage.events.ChatEvents;
import com.fivemanage.events.GameplayEvents;
import com.fivemanage.events.ServerLifecycleEvents;
import com.fivemanage.events.DeathSystem;
import com.fivemanage.events.PlayerEvents;
import com.fivemanage.events.ecs.*;
import com.hypixel.hytale.builtin.adventure.objectives.events.TreasureChestOpeningEvent;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

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

        if (logProviderConfig == null || logProviderConfig.getConfig().isEmpty()) {
            internalLogger.atWarning().log("No logging configuration found, skipping plugin setup.");
            return;
        }

        internalLogger.atInfo().log("Config loaded - Provider: " + logProviderConfig.getProvider());
        FivemanageLogger.initialize(logProviderConfig.getProvider(), logProviderConfig.getConfig());
        internalLogger.atInfo().log("FivemanageLogger initialized with provider: " + logProviderConfig.getProvider());

        EventsConfig events = config.get().getEventsConfig();

        if (events.getServerLifecycle().isEnabled()) {
            ServerLifecycleEvents.setDataset(events.getServerLifecycle().getDataset());
            this.getEventRegistry().registerGlobal(ShutdownEvent.class, ServerLifecycleEvents::onShutdown);
            ServerLifecycleEvents.onServerStarted();
            internalLogger.atInfo().log("Registered: ServerLifecycle events");
        }

        if (events.getPlayerEvents().isEnabled()) {
            PlayerEvents.setDataset(events.getPlayerEvents().getDataset());
            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEvents::onPlayerReady);
            this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, PlayerEvents::onPlayerConnect);
            this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerEvents::onPlayerDisconnect);
            internalLogger.atInfo().log("Registered: PlayerEvents (connect, ready, disconnect)");
        }


        if (events.getChat().isEnabled()) {
            ChatEvents.setDataset(events.getChat().getDataset());
            this.getEventRegistry().registerAsyncGlobal(PlayerChatEvent.class, future ->
                future.thenApply(event -> {
                    ChatEvents.onPlayerChat(event);
                    return event;
                })
            );
            internalLogger.atInfo().log("Registered: Chat events");
        }

        if (events.getCombat().isEnabled()) {
            DeathSystem deathSystem = new DeathSystem();
            deathSystem.setDataset(events.getCombat().getDataset());
            this.getEntityStoreRegistry().registerSystem(deathSystem);
            internalLogger.atInfo().log("Registered: Combat events (death system)");
        }
        if (events.getBlockEvents().isEnabled()) {
            String ds = events.getBlockEvents().getDataset();
            BreakBlockEvent.setDataset(ds);
            PlaceBlockHandler.setDataset(ds);
            this.getEntityStoreRegistry().registerSystem(new BreakBlockEvent());
            this.getEntityStoreRegistry().registerSystem(new PlaceBlockHandler());
            internalLogger.atInfo().log("Registered: Block events (break, place)");
        }

        if (events.getGameplay().isEnabled()) {
            String ds = events.getGameplay().getDataset();
            GameplayEvents.setDataset(ds);
            this.getEventRegistry().registerGlobal(TreasureChestOpeningEvent.class, GameplayEvents::onTreasureChestOpening);
            this.getEntityStoreRegistry().registerSystem(new CraftRecipeHandler(ds));
            this.getEntityStoreRegistry().registerSystem(new ChangeGameModeHandler(ds));
            internalLogger.atInfo().log("Registered: Gameplay events (craft, treasure, game mode)");
        }

        if (events.getExploration().isEnabled()) {
            String ds = events.getExploration().getDataset();
            this.getEntityStoreRegistry().registerSystem(new DiscoverZoneHandler(ds));
            this.getEntityStoreRegistry().registerSystem(new DiscoverInstanceHandler(ds));
            internalLogger.atInfo().log("Registered: Exploration events (zone, instance discovery)");
        }

        internalLogger.atInfo().log("Fivemanage Logger fully initialized");
    }

    @Override
    protected void shutdown() {
        internalLogger.atInfo().log("Flushing logs before shutdown...");
        FivemanageLogger.shutdown();
    }
}
