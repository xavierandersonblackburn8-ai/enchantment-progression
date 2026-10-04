package com.dark.enchantmentprogression.network;

import com.dark.enchantmentprogression.EnchantmentProgression;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpgradeRequest(long requestId, Identifier enchantmentId) implements CustomPacketPayload {
    public static final Type<UpgradeRequest> TYPE = new Type<>(Identifier.fromNamespaceAndPath(EnchantmentProgression.MOD_ID, "upgrade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeRequest> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> { buf.writeVarLong(value.requestId); buf.writeIdentifier(value.enchantmentId); },
            buf -> new UpgradeRequest(buf.readVarLong(), buf.readIdentifier())
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
