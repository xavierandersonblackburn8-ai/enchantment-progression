package com.dark.enchantmentprogression;

import com.dark.enchantmentprogression.network.UpgradeRequest;
import com.dark.enchantmentprogression.network.UpgradeResult;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EnchantmentProgression implements ModInitializer {
    public static final String MOD_ID = "enchantment_progression";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(UpgradeRequest.TYPE, UpgradeRequest.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UpgradeResult.TYPE, UpgradeResult.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UpgradeRequest.TYPE, (payload, context) -> {
            UpgradeResult result = ServerUpgradeService.tryUpgrade(context.player(), payload.requestId(), payload.enchantmentId());
            ServerPlayNetworking.send(context.player(), result);
        });
        LOGGER.info("Enchantment Progression initialized");
    }
}
