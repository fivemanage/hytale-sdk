package com.fivemanage.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class EventCategoryConfig {
    public static final BuilderCodec<EventCategoryConfig> CODEC =
        BuilderCodec.builder(EventCategoryConfig.class, EventCategoryConfig::new)
            .append(new KeyedCodec<Boolean>("Enabled", Codec.BOOLEAN),
                    (config, value) -> config.enabled = value,
                    (config) -> config.enabled)
            .add()
            .append(new KeyedCodec<String>("Dataset", Codec.STRING),
                    (config, value) -> config.dataset = value,
                    (config) -> config.dataset)
            .add()
            .build();

    private boolean enabled = true;
    private String dataset = "default";

    public EventCategoryConfig() {
    }

    public EventCategoryConfig(boolean enabled, String dataset) {
        this.enabled = enabled;
        this.dataset = dataset;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDataset() {
        return dataset;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }
}
