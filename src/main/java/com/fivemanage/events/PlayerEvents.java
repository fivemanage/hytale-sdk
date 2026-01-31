package com.fivemanage.events;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.fivemanage.FivemanageLogger;
import com.fivemanage.session.PlayerSession;

import java.util.HashMap;
import java.util.Map;

public class PlayerEvents {
    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Player Ready");
        metadata.put("playerName", player.getDisplayName());

        FivemanageLogger.info("default", "player.joinedServer", metadata);
    }

    public static void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef player = event.getPlayerRef();
        PlayerSession.startSession(player.getUuid().toString());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Player Connect");
        metadata.put("username", player.getUsername());

        FivemanageLogger.info("default", "player.connected", metadata);
    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        Long joinTime = PlayerSession.endSession(event.getPlayerRef().getUuid().toString());

        Long sessionDurationSeconds = null;
        Long sessionDurationMs = null;

        if (joinTime != null) {
            sessionDurationMs = System.currentTimeMillis() - joinTime;
            sessionDurationSeconds = sessionDurationMs / 1000;
        }

        PlayerRef player = event.getPlayerRef();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Player Disconnect");
        metadata.put("username", player.getUsername());
        metadata.put("sessionDurationSeconds", sessionDurationSeconds);
        metadata.put("sessionDurationMs", sessionDurationMs);

        FivemanageLogger.info("default", "player.disconnected", metadata);
    }
}