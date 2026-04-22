package com.fivemanage.events.ecs;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.HashMap;
import java.util.Map;

public class CraftRecipeHandler extends EntityEventSystem<EntityStore, CraftRecipeEvent.Post> {
    private final String dataset;

    public CraftRecipeHandler(String dataset) {
        super(CraftRecipeEvent.Post.class);
        this.dataset = dataset;
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl CraftRecipeEvent.Post event) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null || playerRef == null) {
            return;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Player Craft");
        metadata.put("playerName", player.getDisplayName());
        metadata.put("playerId", playerRef.getUuid().toString());

        FivemanageLogger.info(dataset, "player.craft", metadata);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
