package com.fivemanage.events;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.builtin.adventure.objectives.events.TreasureChestOpeningEvent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.Map;

public class GameplayEvents {
    private static String dataset = "default";

    public static void setDataset(String ds) {
        dataset = ds;
    }

    public static void onTreasureChestOpening(TreasureChestOpeningEvent event) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Treasure Chest Opened");
        metadata.put("chestId", event.getChestUUID().toString());
        metadata.put("objectiveId", event.getObjectiveUUID().toString());

        Ref<EntityStore> ref = event.getPlayerRef();
        Store<EntityStore> store = event.getStore();
        if (ref != null && store != null && ref.isValid()) {
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (player != null) {
                metadata.put("playerName", player.getDisplayName());
            }
            if (playerRef != null) {
                metadata.put("playerId", playerRef.getUuid().toString());
            }
        }

        FivemanageLogger.info(dataset, "player.openTreasure", metadata);
    }
}
