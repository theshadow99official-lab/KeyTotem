package com.shadow.keytotem.mixin;

import com.shadow.keytotem.packets.OptOutPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

@Mixin(MinecraftClient.class)
public class ServerOptOut {
    @Unique
    private static boolean handledOptOut = false;

    // The original registers a handler so servers can opt-out; package/imports updated only.
    // Keep behavior identical to original autototem's ServerOptOut.
    static {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            handledOptOut = false;
        });

        // Does not alter any behavior; ensures server can signal opt-out if needed.
        ServerPlayNetworking.registerGlobalReceiver(OptOutPacket.ID, (server, player, handler, buf, sender) -> {
            handledOptOut = true;
        });
    }
}