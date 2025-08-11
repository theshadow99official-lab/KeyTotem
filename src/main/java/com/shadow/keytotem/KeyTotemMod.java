package com.shadow.keytotem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
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
        totemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_TOTEM,
                GLFW.GLFW_KEY_G, // Default key: G
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (totemKey.wasPressed()) {
                equipTotem(client);
            }
        });
    }

    private void equipTotem(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        PlayerEntity player = client.player;

        // If already holding a totem in offhand, do nothing
        if (player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        int slotWithTotem = -1;

        // Find a totem in inventory (0–35 are main inventory, hotbar)
        for (int i = 0; i < player.getInventory().main.size(); i++) {
            ItemStack stack = player.getInventory().main.get(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                slotWithTotem = i;
                break;
            }
        }

        if (slotWithTotem != -1) {
            // Swap using clickSlot so server syncs the change
            int inventorySlot = slotWithTotem < 9 ? slotWithTotem + 36 : slotWithTotem; // Convert hotbar index
            client.interactionManager.clickSlot(
                    player.currentScreenHandler.syncId,
                    inventorySlot,
                    40, // 40 = offhand slot
                    SlotActionType.SWAP,
                    player
            );

            // Show feedback
            client.inGameHud.setOverlayMessage(Text.literal("Totem equipped!"), false);

        } else {
            // No totem found
            SystemToast.add(
                    client.getToastManager(),
                    SystemToast.Type.PERIODIC_NOTIFICATION, // Safe toast type
                    Text.literal("No Totem Found"),
                    Text.literal("No Totem Available")
            );
        }
    }
}
