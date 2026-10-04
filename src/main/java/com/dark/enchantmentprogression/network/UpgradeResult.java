package com.dark.enchantmentprogression.network;

import com.dark.enchantmentprogression.EnchantmentProgression;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpgradeResult(long requestId, Identifier enchantmentId, boolean success, int newLevel, int xpCost, int remainingXpLevels, String reason) implements CustomPacketPayload {
    public static final Type<UpgradeResult> TYPE = new Type<>(Identifier.fromNamespaceAndPath(EnchantmentProgression.MOD_ID, "upgrade_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeResult> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> { buf.writeVarLong(value.requestId); buf.writeIdentifier(value.enchantmentId); buf.writeBoolean(value.success); buf.writeVarInt(value.newLevel); buf.writeVarInt(value.xpCost); buf.writeVarInt(value.remainingXpLevels); buf.writeUtf(value.reason, 64); },
            buf -> new UpgradeResult(buf.readVarLong(), buf.readIdentifier(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(64))
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
