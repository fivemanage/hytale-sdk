package com.fivemanage.events;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class ChatEvents {
    private static String dataset = "default";

    public static void setDataset(String ds) {
        dataset = ds;
    }

    public static void onPlayerChat(PlayerChatEvent event) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Player Chat");
        metadata.put("playerName", event.getSender().getUsername());
        metadata.put("playerId", event.getSender().getUuid().toString());
        metadata.put("message", event.getContent());

        FivemanageLogger.info(dataset, "player.chat", metadata);
    }
}
