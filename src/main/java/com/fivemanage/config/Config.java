package com.fivemanage.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class Config {
    public static final BuilderCodec<Config> CODEC =
        BuilderCodec.builder(Config.class, Config::new)
            .append(new KeyedCodec<LogProviderConfig>("LogProvider", LogProviderConfig.CODEC),
                    (config, value) -> config.logProviderConfig = value,
                    (config) -> config.logProviderConfig)
            .add()
            .append(new KeyedCodec<EventsConfig>("Events", EventsConfig.CODEC),
                    (config, value) -> config.eventsConfig = value,
                    (config) -> config.eventsConfig)
            .add()
            .build();

    private LogProviderConfig logProviderConfig = new LogProviderConfig();
    private EventsConfig eventsConfig = new EventsConfig();

    public Config() {
    }

    public LogProviderConfig getLogProviderConfig() {
        return logProviderConfig;
    }

    public EventsConfig getEventsConfig() {
        return eventsConfig;
    }

    public void setLogProviderConfig(LogProviderConfig logProviderConfig) {
        this.logProviderConfig = logProviderConfig;
    }

    public void setEventsConfig(EventsConfig eventsConfig) {
        this.eventsConfig = eventsConfig;
    }
}
