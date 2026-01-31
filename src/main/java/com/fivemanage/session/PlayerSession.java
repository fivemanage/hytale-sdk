package com.fivemanage.session;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSession {
    private static final Map<String, Long> playerSessions = new ConcurrentHashMap<>();

    public static void startSession(String playerId) {
        playerSessions.put(playerId, System.currentTimeMillis());
    }

    public static Long endSession(String playerId) {
        Long joinTime = playerSessions.remove(playerId);
        return joinTime;
    }
}