package com.fivemanage.events;

import com.fivemanage.FivemanageLogger;
import com.hypixel.hytale.protocol.ItemQuantity;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.inventory.DropCreativeItem;
import com.hypixel.hytale.protocol.packets.inventory.DropItemStack;
import com.hypixel.hytale.protocol.packets.inventory.InventoryAction;
import com.hypixel.hytale.protocol.packets.inventory.MoveItemStack;
import com.hypixel.hytale.protocol.packets.inventory.SetCreativeItem;
import com.hypixel.hytale.protocol.packets.inventory.SmartGiveCreativeItem;
import com.hypixel.hytale.protocol.packets.inventory.SmartMoveItemStack;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import com.hypixel.hytale.protocol.packets.window.ChangeBlockAction;
import com.hypixel.hytale.protocol.packets.window.CraftItemAction;
import com.hypixel.hytale.protocol.packets.window.CraftRecipeAction;
import com.hypixel.hytale.protocol.packets.window.SelectSlotAction;
import com.hypixel.hytale.protocol.packets.window.SendWindowAction;
import com.hypixel.hytale.protocol.packets.window.SetActiveAction;
import com.hypixel.hytale.protocol.packets.window.SortItemsAction;
import com.hypixel.hytale.protocol.packets.window.UpdateCategoryAction;
import com.hypixel.hytale.protocol.packets.window.WindowAction;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.HashMap;
import java.util.Map;

public class InventoryEvents {
    private static String dataset = "default";
    private static PacketFilter inboundFilter;

    public static synchronized void register(String ds) {
        dataset = ds;
        unregister();
        inboundFilter = PacketAdapters.registerInbound((PlayerPacketWatcher) InventoryEvents::onInboundPacket);
    }

    public static synchronized void unregister() {
        if (inboundFilter != null) {
            PacketAdapters.deregisterInbound(inboundFilter);
            inboundFilter = null;
        }
    }

    private static void onInboundPacket(PlayerRef playerRef, Packet packet) {
        if (playerRef == null || packet == null) {
            return;
        }

        if (packet instanceof MoveItemStack move) {
            Map<String, Object> metadata = baseMetadata(playerRef, packet, "Move Item Stack");
            addSection(metadata, "from", move.fromSectionId);
            metadata.put("fromSlotId", move.fromSlotId);
            addSection(metadata, "to", move.toSectionId);
            metadata.put("toSlotId", move.toSlotId);
            metadata.put("quantity", move.quantity);
            FivemanageLogger.info(dataset, "inventory.move", metadata);
            return;
        }

        if (packet instanceof SmartMoveItemStack move) {
            Map<String, Object> metadata = baseMetadata(playerRef, packet, "Smart Move Item Stack");
            addSection(metadata, "from", move.fromSectionId);
            metadata.put("fromSlotId", move.fromSlotId);
            metadata.put("quantity", move.quantity);
            metadata.put("moveType", enumName(move.moveType));
            FivemanageLogger.info(dataset, "inventory.smartMove", metadata);
            return;
        }

        if (packet instanceof DropItemStack drop) {
            Map<String, Object> metadata = baseMetadata(playerRef, packet, "Drop Item Stack");
            addSection(metadata, "inventory", drop.inventorySectionId);
            metadata.put("slotId", drop.slotId);
            metadata.put("quantity", drop.quantity);
            FivemanageLogger.info(dataset, "inventory.drop", metadata);
            return;
        }

        if (packet instanceof InventoryAction inventoryAction) {
            Map<String, Object> metadata = baseMetadata(playerRef, packet, "Inventory Action");
            addSection(metadata, "inventory", inventoryAction.inventorySectionId);
            metadata.put("inventoryActionType", enumName(inventoryAction.inventoryActionType));
            metadata.put("actionData", inventoryAction.actionData);
            FivemanageLogger.info(dataset, "inventory.action", metadata);
            return;
        }

        if (packet instanceof UpdatePlayerInventory updatePlayerInventory) {
            Map<String, Object> metadata = baseMetadata(playerRef, packet, "Update Player Inventory");
            metadata.put("hasStorage", updatePlayerInventory.storage != null);
            metadata.put("hasArmor", updatePlayerInventory.armor != null);
            metadata.put("hasHotbar", updatePlayerInventory.hotbar != null);
            metadata.put("hasUtility", updatePlayerInventory.utility != null);
            metadata.put("hasTools", updatePlayerInventory.tools != null);
            metadata.put("hasBackpack", updatePlayerInventory.backpack != null);
            FivemanageLogger.info(dataset, "inventory.update", metadata);
            return;
        }

        if (packet instanceof SendWindowAction sendWindowAction) {
            logWindowAction(playerRef, sendWindowAction);
        }
    }

    private static void logWindowAction(PlayerRef playerRef, SendWindowAction packet) {
        WindowAction action = packet.action;
        if (!isInventoryWindowAction(action)) {
            return;
        }

        Map<String, Object> metadata = baseMetadata(playerRef, packet, "Inventory Window Action");
        metadata.put("windowId", packet.id);
        metadata.put("windowActionType", action.getClass().getSimpleName());

        if (action instanceof SelectSlotAction selectSlotAction) {
            metadata.put("slot", selectSlotAction.slot);
        } else if (action instanceof SetActiveAction setActiveAction) {
            metadata.put("state", setActiveAction.state);
        } else if (action instanceof ChangeBlockAction changeBlockAction) {
            metadata.put("down", changeBlockAction.down);
        } else if (action instanceof CraftRecipeAction craftRecipeAction) {
            metadata.put("recipeId", craftRecipeAction.recipeId);
            metadata.put("quantity", craftRecipeAction.quantity);
        } else if (action instanceof UpdateCategoryAction updateCategoryAction) {
            metadata.put("category", updateCategoryAction.category);
            metadata.put("itemCategory", updateCategoryAction.itemCategory);
        }

        FivemanageLogger.info(dataset, "inventory.windowAction", metadata);
    }

    private static boolean isInventoryWindowAction(WindowAction action) {
        return action instanceof SelectSlotAction
            || action instanceof SortItemsAction
            || action instanceof SetActiveAction
            || action instanceof ChangeBlockAction
            || action instanceof CraftItemAction
            || action instanceof CraftRecipeAction
            || action instanceof UpdateCategoryAction;
    }

    private static Map<String, Object> baseMetadata(PlayerRef playerRef, Packet packet, String action) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("action", action);
        metadata.put("playerName", playerRef.getUsername());
        metadata.put("playerId", playerRef.getUuid().toString());
        metadata.put("packetId", packet.getId());
        metadata.put("packetName", packet.getClass().getSimpleName());
        return metadata;
    }

    private static void addSection(Map<String, Object> metadata, String prefix, int sectionId) {
        metadata.put(prefix + "SectionId", sectionId);
        metadata.put(prefix + "SectionName", sectionName(sectionId));
        metadata.put(prefix + "IsOpenContainer", sectionId >= 0);
    }

    private static String sectionName(int sectionId) {
        return switch (sectionId) {
            case InventoryComponent.HOTBAR_SECTION_ID -> "hotbar";
            case InventoryComponent.STORAGE_SECTION_ID -> "storage";
            case InventoryComponent.ARMOR_SECTION_ID -> "armor";
            case InventoryComponent.UTILITY_SECTION_ID -> "utility";
            case InventoryComponent.TOOLS_SECTION_ID -> "tools";
            case InventoryComponent.BACKPACK_SECTION_ID -> "backpack";
            default -> sectionId >= 0 ? "openContainer" : "unknown";
        };
    }

    private static Map<String, Object> itemQuantityMetadata(ItemQuantity item) {
        if (item == null) {
            return null;
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("itemId", item.itemId);
        metadata.put("quantity", item.quantity);
        return metadata;
    }

    private static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
