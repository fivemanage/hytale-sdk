package com.fivemanage.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class PlayerEventsConfig {
    public static final BuilderCodec<PlayerEventsConfig> CODEC =
        BuilderCodec.builder(PlayerEventsConfig.class, PlayerEventsConfig::new)
            .append(new KeyedCodec<String>("Dataset", Codec.STRING),
                    (config, value) -> config.dataset = value,
                    (config) -> config.dataset)
            .add()
            .append(new KeyedCodec<Boolean>("Enabled", Codec.BOOLEAN),
                    (config, value) -> config.enabled = value,
                    (config) -> config.enabled)
            .add()
            .build();

    private String dataset = "default";
    private Boolean enabled = true;

    public PlayerEventsConfig() {
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
