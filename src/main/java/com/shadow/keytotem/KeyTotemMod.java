package com.example.keytotem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeyTotemMod implements ClientModInitializer {

    private static KeyBinding totemKey;

    @Override
    public void onInitializeClient() {
        // Register keybinding
        totemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.keytotem.equip_totem", // translation key
                GLFW.GLFW_KEY_R, // default key (changeable in controls)
                "category.keytotem" // category in keybind menu
        ));

        // Listen for key presses each client tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (totemKey.wasPressed()) {
                equipTotem(client);
            }
        });
    }

    private void equipTotem(MinecraftClient client) {
        if (client.player == null) return;
        PlayerEntity player = client.player;

        // If offhand already has a totem, skip
        if (isTotem(player.getOffHandStack())) {
            return;
        }

        // Search hotbar + inventory
        int slotWithTotem = findTotemSlot(player);
        if (slotWithTotem != -1) {
            // Move totem to offhand
            ItemStack found = player.getInventory().getStack(slotWithTotem).copy();
            player.getInventory().removeStack(slotWithTotem, 1);
            player.getInventory().offHand.set(0, found);

            showToast(client, Text.literal("Totem equipped"));
        } else {
            showToast(client, Text.literal("No Totem found"));
        }
    }

    private boolean isTotem(ItemStack stack) {
        return stack != null && stack.getItem() == Items.TOTEM_OF_UNDYING;
    }

    private int findTotemSlot(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (isTotem(player.getInventory().getStack(i))) {
                return i;
            }
        }
        return -1;
    }

    private void showToast(MinecraftClient client, Text message) {
        client.getToastManager().add(new SystemToast(
                SystemToast.Type.TUTORIAL_HINT,
                Text.literal("Key Totem"),
                message
        ));
    }
}
