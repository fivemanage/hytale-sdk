package com.fivemanage.events.ecs;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.InventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryChangeHandler extends EntityEventSystem<EntityStore, InventoryChangeEvent> {
    private final String dataset;

    public InventoryChangeHandler(String dataset) {
        super(InventoryChangeEvent.class);
        this.dataset = dataset;
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl InventoryChangeEvent event) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Transaction transaction = event.getTransaction();

        if (playerRef == null || transaction == null || !transaction.succeeded()) {
            return;
        }

        List<Map<String, Object>> slotChanges = new ArrayList<>();
        collectSlotChanges(transaction, slotChanges);

        if (slotChanges.isEmpty()) {
            Map<String, Object> metadata = baseMetadata(playerRef, event);
            metadata.put("transactionType", transaction.getClass().getSimpleName());
            metadata.put("transaction", transaction.toString());
            FivemanageLogger.info(dataset, "inventory.changed", metadata);
            return;
        }

        for (Map<String, Object> slotChange : slotChanges) {
            Map<String, Object> metadata = baseMetadata(playerRef, event);
            metadata.put("slotChange", slotChange);
            FivemanageLogger.info(dataset, messageFor(slotChange.get("actionType")), metadata);
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    private static Map<String, Object> baseMetadata(PlayerRef playerRef, InventoryChangeEvent event) {
        Map<String, Object> metadata = new HashMap<>();
        int sectionId = sectionIdFor(event.getComponentType());
        metadata.put("action", "Inventory Changed");
        metadata.put("playerName", playerRef.getUsername());
        metadata.put("playerId", playerRef.getUuid().toString());
        metadata.put("sectionId", sectionId);
        metadata.put("sectionName", sectionName(sectionId));
        metadata.put("componentType", event.getComponentType().toString());
        return metadata;
    }

    private static void collectSlotChanges(Transaction transaction, List<Map<String, Object>> slotChanges) {
        if (transaction == null) {
            return;
        }

        if (transaction instanceof ListTransaction<?> listTransaction) {
            for (Transaction child : listTransaction.getList()) {
                collectSlotChanges(child, slotChanges);
            }
            return;
        }

        if (transaction instanceof MoveTransaction<?> moveTransaction) {
            collectSlotChanges(moveTransaction.getRemoveTransaction(), slotChanges);
            collectSlotChanges(moveTransaction.getAddTransaction(), slotChanges);
            return;
        }

        if (transaction instanceof ItemStackTransaction itemStackTransaction) {
            for (SlotTransaction slotTransaction : itemStackTransaction.getSlotTransactions()) {
                collectSlotChanges(slotTransaction, slotChanges);
            }
            return;
        }

        if (transaction instanceof SlotTransaction slotTransaction) {
            slotChanges.add(slotChangeMetadata(slotTransaction));
        }
    }

    private static Map<String, Object> slotChangeMetadata(SlotTransaction transaction) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("actionType", transaction.getAction() == null ? "UNKNOWN" : transaction.getAction().name());
        metadata.put("slot", transaction.getSlot());
        metadata.put("slotBefore", itemStackMetadata(transaction.getSlotBefore()));
        metadata.put("slotAfter", itemStackMetadata(transaction.getSlotAfter()));
        metadata.put("output", itemStackMetadata(transaction.getOutput()));
        metadata.put("quantityDelta", quantityDelta(transaction.getSlotBefore(), transaction.getSlotAfter()));
        return metadata;
    }

    private static String messageFor(Object actionType) {
        if ("ADD".equals(actionType)) {
            return "inventory.added";
        }
        if ("REMOVE".equals(actionType)) {
            return "inventory.removed";
        }
        return "inventory.changed";
    }

    private static Map<String, Object> itemStackMetadata(ItemStack itemStack) {
        if (ItemStack.isEmpty(itemStack)) {
            return null;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("itemId", itemStack.getItemId());
        metadata.put("quantity", itemStack.getQuantity());

        String blockKey = itemStack.getBlockKey();
        if (blockKey != null) {
            metadata.put("blockKey", blockKey);
        }

        if (itemStack.getMaxDurability() > 0) {
            metadata.put("durability", itemStack.getDurability());
            metadata.put("maxDurability", itemStack.getMaxDurability());
        }

        return metadata;
    }

    private static Integer quantityDelta(ItemStack before, ItemStack after) {
        int beforeQuantity = ItemStack.isEmpty(before) ? 0 : before.getQuantity();
        int afterQuantity = ItemStack.isEmpty(after) ? 0 : after.getQuantity();
        return afterQuantity - beforeQuantity;
    }

    private static int sectionIdFor(ComponentType<EntityStore, ? extends InventoryComponent> componentType) {
        if (componentType == null) {
            return Integer.MIN_VALUE;
        }

        if (componentType.equals(InventoryComponent.Hotbar.getComponentType())) {
            return InventoryComponent.HOTBAR_SECTION_ID;
        }
        if (componentType.equals(InventoryComponent.Storage.getComponentType())) {
            return InventoryComponent.STORAGE_SECTION_ID;
        }
        if (componentType.equals(InventoryComponent.Armor.getComponentType())) {
            return InventoryComponent.ARMOR_SECTION_ID;
        }
        if (componentType.equals(InventoryComponent.Utility.getComponentType())) {
            return InventoryComponent.UTILITY_SECTION_ID;
        }
        if (componentType.equals(InventoryComponent.Tool.getComponentType())) {
            return InventoryComponent.TOOLS_SECTION_ID;
        }
        if (componentType.equals(InventoryComponent.Backpack.getComponentType())) {
            return InventoryComponent.BACKPACK_SECTION_ID;
        }
        return Integer.MIN_VALUE;
    }

    private static String sectionName(int sectionId) {
        return switch (sectionId) {
            case InventoryComponent.HOTBAR_SECTION_ID -> "hotbar";
            case InventoryComponent.STORAGE_SECTION_ID -> "storage";
            case InventoryComponent.ARMOR_SECTION_ID -> "armor";
            case InventoryComponent.UTILITY_SECTION_ID -> "utility";
            case InventoryComponent.TOOLS_SECTION_ID -> "tools";
            case InventoryComponent.BACKPACK_SECTION_ID -> "backpack";
            default -> "unknown";
        };
    }
}
