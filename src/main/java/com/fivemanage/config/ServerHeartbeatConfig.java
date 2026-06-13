package com.fivemanage.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class ServerHeartbeatConfig {
    public static final BuilderCodec<ServerHeartbeatConfig> CODEC =
        BuilderCodec.builder(ServerHeartbeatConfig.class, ServerHeartbeatConfig::new)
            .append(new KeyedCodec<Boolean>("Enabled", Codec.BOOLEAN),
                    (config, value) -> config.enabled = value,
                    (config) -> config.enabled)
            .add()
            .append(new KeyedCodec<String>("Dataset", Codec.STRING),
                    (config, value) -> config.dataset = value,
                    (config) -> config.dataset)
            .add()
            .append(new KeyedCodec<Integer>("IntervalMs", Codec.INTEGER),
                    (config, value) -> config.intervalMs = value,
                    (config) -> config.intervalMs)
            .add()
            .build();

    private boolean enabled = true;
    private String dataset = "server-metrics";
    private int intervalMs = 30000;

    public ServerHeartbeatConfig() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDataset() {
        return dataset;
    }

    public int getIntervalMs() {
        return intervalMs;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public void setIntervalMs(int intervalMs) {
        this.intervalMs = intervalMs;
    }
}
