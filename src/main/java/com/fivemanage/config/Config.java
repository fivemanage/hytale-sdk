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
            .append(new KeyedCodec<PlayerEventsConfig>("PlayerEvents", PlayerEventsConfig.CODEC),
                    (config, value) -> config.playerEventsConfig = value,
                    (config) -> config.playerEventsConfig)
            .add()
            .build();

    private LogProviderConfig logProviderConfig = new LogProviderConfig();
    private PlayerEventsConfig playerEventsConfig = new PlayerEventsConfig();

    public Config() {
    }


    public LogProviderConfig getLogProviderConfig() {
        return logProviderConfig;
    }

    public PlayerEventsConfig getPlayerEventsConfig() {
        return playerEventsConfig;
    }

    public void setLogProviderConfig(LogProviderConfig logProviderConfig) {
        this.logProviderConfig = logProviderConfig;
    }
}
