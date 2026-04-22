package com.fivemanage.events.ecs;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.builtin.instances.event.DiscoverInstanceEvent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.HashMap;
import java.util.Map;

public class DiscoverInstanceHandler extends EntityEventSystem<EntityStore, DiscoverInstanceEvent.Display> {
    private final String dataset;

    public DiscoverInstanceHandler(String dataset) {
        super(DiscoverInstanceEvent.Display.class);
        this.dataset = dataset;
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl DiscoverInstanceEvent.Display event) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null || playerRef == null) {
            return;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Discover Instance");
        metadata.put("playerName", player.getDisplayName());
        metadata.put("playerId", playerRef.getUuid().toString());
        metadata.put("instanceWorldId", event.getInstanceWorldUuid().toString());

        FivemanageLogger.info(dataset, "player.discoverInstance", metadata);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
