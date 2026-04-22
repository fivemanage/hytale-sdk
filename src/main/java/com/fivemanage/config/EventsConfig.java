package com.fivemanage.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class EventsConfig {
    public static final BuilderCodec<EventsConfig> CODEC =
        BuilderCodec.builder(EventsConfig.class, EventsConfig::new)
            .append(new KeyedCodec<EventCategoryConfig>("ServerLifecycle", EventCategoryConfig.CODEC),
                    (c, v) -> c.serverLifecycle = v, c -> c.serverLifecycle)
            .add()
            .append(new KeyedCodec<EventCategoryConfig>("PlayerEvents", EventCategoryConfig.CODEC),
                    (c, v) -> c.playerEvents = v, c -> c.playerEvents)
            .add()
            .append(new KeyedCodec<EventCategoryConfig>("Chat", EventCategoryConfig.CODEC),
                    (c, v) -> c.chat = v, c -> c.chat)
            .add()
            .append(new KeyedCodec<EventCategoryConfig>("Combat", EventCategoryConfig.CODEC),
                    (c, v) -> c.combat = v, c -> c.combat)
            .add()
            .append(new KeyedCodec<EventCategoryConfig>("BlockEvents", EventCategoryConfig.CODEC),
                    (c, v) -> c.blockEvents = v, c -> c.blockEvents)
            .add()
            .append(new KeyedCodec<EventCategoryConfig>("Gameplay", EventCategoryConfig.CODEC),
                    (c, v) -> c.gameplay = v, c -> c.gameplay)
            .add()
            .append(new KeyedCodec<EventCategoryConfig>("Exploration", EventCategoryConfig.CODEC),
                    (c, v) -> c.exploration = v, c -> c.exploration)
            .add()
            .build();

    private EventCategoryConfig serverLifecycle = new EventCategoryConfig(true, "default");
    private EventCategoryConfig playerEvents = new EventCategoryConfig(true, "default");
    private EventCategoryConfig chat = new EventCategoryConfig(true, "default");
    private EventCategoryConfig combat = new EventCategoryConfig(true, "default");
    private EventCategoryConfig blockEvents = new EventCategoryConfig(false, "default");
    private EventCategoryConfig gameplay = new EventCategoryConfig(true, "default");
    private EventCategoryConfig exploration = new EventCategoryConfig(true, "default");

    public EventsConfig() {
    }

    public EventCategoryConfig getServerLifecycle() {
        return serverLifecycle;
    }

    public EventCategoryConfig getPlayerEvents() {
        return playerEvents;
    }

    public EventCategoryConfig getChat() {
        return chat;
    }

    public EventCategoryConfig getCombat() {
        return combat;
    }

    public EventCategoryConfig getBlockEvents() {
        return blockEvents;
    }

    public EventCategoryConfig getGameplay() {
        return gameplay;
    }

    public EventCategoryConfig getExploration() {
        return exploration;
    }
}
