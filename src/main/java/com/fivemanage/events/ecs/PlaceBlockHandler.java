package com.fivemanage.events.ecs;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.HashMap;
import java.util.Map;

public class PlaceBlockHandler extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private static String dataset = "default";

    public static void setDataset(String ds) {
        dataset = ds;
    }

    public PlaceBlockHandler() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl PlaceBlockEvent event) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null || playerRef == null) {
            return;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Block Place");
        metadata.put("playerName", player.getDisplayName());
        metadata.put("playerId", playerRef.getUuid().toString());

        ItemStack item = event.getItemInHand();
        if (item != null && !item.isEmpty()) {
            metadata.put("itemId", item.getItemId());
            String blockKey = item.getBlockKey();
            if (blockKey != null) {
                metadata.put("blockKey", blockKey);
            }
        }

        FivemanageLogger.info(dataset, "block.place", metadata);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
