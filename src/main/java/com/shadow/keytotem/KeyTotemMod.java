package com.shadow.keytotem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Simple "auto totem on keypress" mod:
 * - Keybind is registered and visible under Controls -> Keybinds.
 * - On key press, searches player's inventory for Totem of Undying and moves it to offhand.
 * - Does nothing if offhand already has a totem.
 * - No toast is shown if none is found (per your request).
 *
 * IMPORTANT: make sure this package & class match the entrypoint in fabric.mod.json.
 */
public class KeyTotemMod implements ClientModInitializer {

    private static KeyBinding keybinding;

    @Override
    public void onInitializeClient() {
        // Register keybinding (default: G)
        keybinding = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.keytotem.activate",
                        GLFW.GLFW_KEY_G,
                        "category.keytotem"
                )
        );

        // Tick event to poll key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                if (keybinding.wasPressed()) {
                    handleKeyPress(client);
                }
            } catch (Throwable t) {
                // avoid crashing client from our mod
                t.printStackTrace();
            }
        });
    }

    private void handleKeyPress(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        // If offhand already has a totem, skip replacing
        ItemStack offhand = client.player.getOffHandStack();
        if (offhand != null && offhand.getItem() == Items.TOTEM_OF_UNDYING) {
            return; // already equipped
        }

        // Find a totem in player inventory
        PlayerInventory inv = client.player.getInventory();
        int foundInvIndex = -1; // index inside PlayerInventory
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s != null && s.getItem() == Items.TOTEM_OF_UNDYING) {
                foundInvIndex = i;
                break;
            }
        }

        if (foundInvIndex == -1) {
            // No totem found -> per your request do nothing (no toast)
            return;
        }

        // Convert PlayerInventory index to ScreenHandler slot index:
        // Common mapping for PlayerScreenHandler:
        // - Hotbar (inv indices 0..8) map to handler slots 36..44
        // - Main inventory (inv indices 9..35) map to handler slots 9..35
        // - Offhand is slot 45
        int sourceHandlerIndex = mapInventoryIndexToHandler(foundInvIndex);

        // offhand handler slot:
        int offhandHandlerIndex = 45;

        // Do the click sequence:
        // 1) pickup from source slot
        // 2) click offhand slot to place there
        // 3) (optional) if cursor still has item, click source slot again to clear
        try {
            int syncId = client.player.currentScreenHandler.syncId;
            // pickup source
            client.interactionManager.clickSlot(syncId, sourceHandlerIndex, 0, SlotActionType.PICKUP, client.player);
            // place into offhand
            client.interactionManager.clickSlot(syncId, offhandHandlerIndex, 0, SlotActionType.PICKUP, client.player);
            // clear cursor (put remaining stack back into source)
            client.interactionManager.clickSlot(syncId, sourceHandlerIndex, 0, SlotActionType.PICKUP, client.player);
        } catch (IndexOutOfBoundsException ex) {
            // defensive: if handler indexes are different for some clients, avoid crash and print helpful info.
            ex.printStackTrace();
            // Optionally show small client message (action bar) to help debugging:
            if (client.player != null) {
                client.player.sendMessage(Text.of("KeyTotem: failed to move totem (slot mapping issue)."), true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Map PlayerInventory index to ScreenHandler slot index used by clickSlot.
     *
     * Uses common mapping used by PlayerScreenHandler:
     * - Hotbar inv indices 0..8 -> handler 36..44
     * - Main inv indices 9..35 -> handler 9..35 (same)
     *
     * If this mapping is wrong for your environment, adjust accordingly.
     */
    private int mapInventoryIndexToHandler(int invIndex) {
        if (invIndex >= 0 && invIndex <= 8) {
            // hotbar
            return 36 + invIndex;
        } else if (invIndex >= 9 && invIndex <= 35) {
            // main inventory
            return invIndex;
        } else {
            // other slots (armor, offhand) - but we searched only inventory for totem so this shouldn't happen.
            return invIndex;
        }
    }
}
