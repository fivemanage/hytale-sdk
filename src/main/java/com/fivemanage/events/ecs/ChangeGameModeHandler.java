package com.fivemanage.events.ecs;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.event.events.ecs.ChangeGameModeEvent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.HashMap;
import java.util.Map;

public class ChangeGameModeHandler extends EntityEventSystem<EntityStore, ChangeGameModeEvent> {
    private final String dataset;

    public ChangeGameModeHandler(String dataset) {
        super(ChangeGameModeEvent.class);
        this.dataset = dataset;
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl ChangeGameModeEvent event) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null || playerRef == null) {
            return;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", "Game Mode Change");
        metadata.put("playerName", player.getDisplayName());
        metadata.put("playerId", playerRef.getUuid().toString());
        metadata.put("gameMode", event.getGameMode().toString());

        FivemanageLogger.info(dataset, "player.gameModeChange", metadata);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
