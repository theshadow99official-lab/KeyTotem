package com.shadow.keytotem.packets;

import net.fabricmc.fabric.impl.networking.ModernPacketImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadType;

public class OptOutPacket {
    public static final Identifier ID = Identifier.tryParse("autototem-fabric");
    public static final PayloadType<ModernPacketImpl> CODEC = PayloadType.create(
            ID,
            (buf) -> new ModernPacketImpl(buf),
            (packet, buf) -> {
                // packet is empty in original, no payload
            }
    );

    // no runtime data needed — this class only provides ID & codec
}
