package com.dark.enchantmentprogression;

import com.dark.enchantmentprogression.network.UpgradeResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class ServerUpgradeService {
    private static final Map<UUID, Long> LAST_REQUEST_TICK = new ConcurrentHashMap<>();
    private ServerUpgradeService() {}
    public static UpgradeResult tryUpgrade(ServerPlayer player, long requestId, Identifier enchantmentId) {
        long tick = player.level().getGameTime(); Long previous = LAST_REQUEST_TICK.put(player.getUUID(), tick);
        if (previous != null && previous == tick) return fail(requestId, enchantmentId, "rate_limited");
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return fail(requestId, enchantmentId, "no_item");
        if (!EnchantmentHelper.canStoreEnchantments(stack)) return fail(requestId, enchantmentId, "not_enchantable");
        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, enchantmentId);
        Holder.Reference<Enchantment> enchantment = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
        if (enchantment == null) return fail(requestId, enchantmentId, "unknown_enchantment");
        int current = EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
        if (current <= 0 && !enchantment.value().isSupportedItem(stack)) return fail(requestId, enchantmentId, "not_supported");
        if (!ProgressionRules.canAdvance(current)) return fail(requestId, enchantmentId, current, 0, player.experienceLevel, "level_cap");
        UpgradeQuote quote = UpgradeQuote.forCurrentLevel(current); int target = quote.targetLevel(); int cost = quote.xpCost();
        var stored = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (var entry : stored.entrySet()) if (entry.getIntValue() > 0 && !entry.getKey().equals(enchantment) && !Enchantment.areCompatible(entry.getKey(), enchantment)) return fail(requestId, enchantmentId, current, cost, player.experienceLevel, "incompatible");
        if (!UpgradePolicy.canAfford(player.getAbilities().instabuild, player.experienceLevel, cost)) return new UpgradeResult(requestId, enchantmentId, false, current, cost, player.experienceLevel, "not_enough_xp");
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, target));
        int applied = EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
        if (applied != target) { EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, current)); player.getInventory().setChanged(); player.containerMenu.broadcastChanges(); return new UpgradeResult(requestId, enchantmentId, false, current, cost, player.experienceLevel, "write_rejected"); }
        if (UpgradePolicy.shouldCharge(player.getAbilities().instabuild, target, applied)) player.giveExperienceLevels(-cost);
        player.getInventory().setChanged(); player.containerMenu.broadcastChanges();
        return new UpgradeResult(requestId, enchantmentId, true, applied, cost, player.experienceLevel, "ok");
    }
    private static UpgradeResult fail(long requestId, Identifier id, String reason) { return new UpgradeResult(requestId, id, false, 0, 0, -1, reason); }
    private static UpgradeResult fail(long requestId, Identifier id, int currentLevel, int xpCost, int remainingXp, String reason) { return new UpgradeResult(requestId, id, false, currentLevel, xpCost, remainingXp, reason); }
}
