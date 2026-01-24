package com.fivemanage.events.ecs;


import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import com.fivemanage.FivemanageLogger;

import java.util.HashMap;
import java.util.Map;

public class BreakBlockEvent extends EntityEventSystem<EntityStore, com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent> {
    public BreakBlockEvent() {
        super(com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent.class);
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent breakBlockEvent) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Block Break");
        metadata.put("blockType", breakBlockEvent.getBlockType().toString());
        metadata.put("blockId", breakBlockEvent.getBlockType().getId());
        Vector3i targetBlock = breakBlockEvent.getTargetBlock();
        metadata.put("targetBlock", new int[]{targetBlock.x, targetBlock.y, targetBlock.z});
        metadata.put("playerName", player.getDisplayName());
        metadata.put("username", playerRef.getUsername());
        metadata.put("playerId", playerRef.getUuid().toString());

        FivemanageLogger.info("default", "ecs.breakBlock", metadata);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
