package com.shadow.keytotem.client;

import com.shadow.keytotem.packets.OptOutPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class AutototemClient implements ClientModInitializer {

    private static KeyBinding ACTIVATE_KEY;

    @Override
    public void onInitializeClient() {
        // register the opt-out payload type (same as original autototem)
        PayloadTypeRegistry.playC2S().register(OptOutPacket.ID, OptOutPacket.CODEC);

        // register keybinding (R)
        ACTIVATE_KEY = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.keytotem.activate", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.keytotem")
        );

        // listen for key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ACTIVATE_KEY.wasPressed()) {
                tryEquipTotem(client);
            }
        });
    }

    private void tryEquipTotem(MinecraftClient client) {
        if (client.player == null || client.getNetworkHandler() == null) return;

        // don't do anything if offhand already has a totem
        if (client.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        PlayerInventory inv = client.player.getInventory();

        int totemSlot = -1;
        // iterate over the player's inventory (main includes hotbar first)
        for (int i = 0; i < inv.main.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }

        if (totemSlot == -1) {
            // no totem found; do nothing (user requested no toast)
            return;
        }

        List<Packet<?>> packets = new ArrayList<>();

        if (totemSlot < 9) { // hotbar slot
            int previous = inv.selectedSlot;
            packets.add(new UpdateSelectedSlotC2SPacket(totemSlot));
            packets.add(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND));
            packets.add(new UpdateSelectedSlotC2SPacket(previous));
        } else { // inventory slot (non-hotbar)
            packets.add(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND));
            packets.add(new PickFromInventoryC2SPacket(totemSlot));
            packets.add(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND));
        }

        for (Packet<?> p : packets) {
            client.getNetworkHandler().sendPacket(p);
        }
    }
}
