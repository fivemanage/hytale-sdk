package com.fivemanage.events;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class DeathSystem extends DeathSystems.OnDeathSystem {
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull DeathComponent component,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Player playerComponent = store.getComponent(ref, Player.getComponentType());
        UUIDComponent uuid = store.getComponent(ref, UUIDComponent.getComponentType());
        // not sure if we should just do this without anything else
        assert playerComponent != null;

        Map<String, Object> metadata = new HashMap<>();

        metadata.put("playerName", playerComponent.getDisplayName());
        metadata.put("playerId", uuid.getUuid().toString());

        var deathInfo = component.getDeathInfo();
        Damage.Source source = deathInfo.getSource();
        if (source instanceof Damage.EntitySource) {
            Damage.EntitySource entitySource = (Damage.EntitySource) source;
            Ref<EntityStore> killerRef = entitySource.getRef();

            if (killerRef.isValid()) {
                Player killer = store.getComponent(killerRef, Player.getComponentType());

                if (killer != null) {
                    UUIDComponent killerUuid = store.getComponent(killerRef, UUIDComponent.getComponentType());

                    metadata.put("killerName", killer.getDisplayName());
                    metadata.put("killerId", killerUuid.getUuid().toString());
                } else {
                    NPCEntity npc = store.getComponent(killerRef, NPCEntity.getComponentType());
                    if (npc != null) {
                        // these seem to be the same thing
                        //metadata.put("npcTypeId", npc.getNPCTypeId());
                        //metadata.put("npcRoleName", npc.getRoleName());

                        metadata.put("killedBy", npc.getRoleName());
                        // maybe valuable?
                        metadata.put("killedByNpc", true);
                    }
                }
            }
        }

        if (deathInfo != null) {
            // death amount is the amount of damage taken before death?
            var deathAmount = deathInfo.getAmount();
            metadata.put("deathAmount", deathAmount);
        }

        // returns stuff like
        // deathMessage.messageParams.damageSource.monospace	Null
        // deathMessage.messageParams.damageSource.underlined	Null
      /*  var deathMessage = component.getDeathMessage();
        if (deathMessage != null) {
            metadata.put("deathMessage", deathMessage.getFormattedMessage());
        } */

        var deathCause = component.getDeathCause();
        if (deathCause != null) {
            // IDS:
            // Physical: Likley to be a player/npc with a weapon
            metadata.put("deathCauseId", deathCause.getId());
        }

        FivemanageLogger.debug("default", "player.died: " + playerComponent.getDisplayName(), metadata);
    }
}
