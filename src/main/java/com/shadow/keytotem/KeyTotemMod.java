package com.shadow.keytotem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeyTotemMod implements ClientModInitializer {

    private static final String KEY_CATEGORY = "key.categories.keytotem";
    private static final String KEY_TOTEM = "key.keytotem.activate";
    private static KeyBinding totemKey;

    @Override
    public void onInitializeClient() {
        // Register keybinding
        totemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_TOTEM,
                GLFW.GLFW_KEY_G, // Default key
                KEY_CATEGORY
        ));

        // Check each tick for key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (totemKey.wasPressed()) {
                equipTotem(client);
            }
        });
    }

    private void equipTotem(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        PlayerEntity player = client.player;

        // Skip if already holding a totem in offhand
        if (player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        int slotWithTotem = -1;

        // Search inventory for a real totem
        for (int i = 0; i < player.getInventory().main.size(); i++) {
            ItemStack stack = player.getInventory().main.get(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                slotWithTotem = i;
                break;
            }
        }

        if (slotWithTotem != -1) {
            // Convert hotbar index to container slot index
            int inventorySlot = slotWithTotem < 9 ? slotWithTotem + 36 : slotWithTotem;

            // Swap with offhand using clickSlot (server sync)
            client.interactionManager.clickSlot(
                    player.currentScreenHandler.syncId,
                    inventorySlot,
                    45, // Offhand slot index
                    SlotActionType.SWAP,
                    player
            );

            // Optional on-screen feedback
            client.inGameHud.setOverlayMessage(Text.literal("Totem equipped!"), false);
        }
    }
}
