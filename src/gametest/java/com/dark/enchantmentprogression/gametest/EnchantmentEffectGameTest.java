package com.dark.enchantmentprogression.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class EnchantmentEffectGameTest {
    private static Holder.Reference<Enchantment> enchantment(GameTestHelper helper, String path) {
        ResourceKey<Enchantment> key = ResourceKey.create(
                Registries.ENCHANTMENT,
                Identifier.fromNamespaceAndPath("minecraft", path)
        );
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElseThrow();
    }

    private static ItemStack enchanted(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment, level));
        return stack;
    }

    @GameTest
    public void sharpnessAboveVanillaMaxIncreasesRealDamage(GameTestHelper helper) {
        Holder.Reference<Enchantment> sharpness = enchantment(helper, "sharpness");
        Player target = helper.makeMockPlayer();
        var source = helper.getLevel().damageSources().generic();

        ItemStack sharpnessFive = enchanted(new ItemStack(Items.DIAMOND_SWORD), sharpness, 5);
        ItemStack sharpnessSix = enchanted(new ItemStack(Items.DIAMOND_SWORD), sharpness, 6);

        int storedFive = EnchantmentHelper.getItemEnchantmentLevel(sharpness, sharpnessFive);
        int storedSix = EnchantmentHelper.getItemEnchantmentLevel(sharpness, sharpnessSix);
        float damageFive = EnchantmentHelper.modifyDamage(helper.getLevel(), sharpnessFive, target, source, 0.0F);
        float damageSix = EnchantmentHelper.modifyDamage(helper.getLevel(), sharpnessSix, target, source, 0.0F);

        if (storedFive != 5 || storedSix != 6) {
            helper.fail("Above-cap Sharpness level was not stored exactly: " + storedFive + ", " + storedSix);
            return;
        }
        if (!(damageSix > damageFive)) {
            helper.fail("Sharpness VI must produce more real damage than Sharpness V: V=" + damageFive + " VI=" + damageSix);
            return;
        }
        if (Math.abs(damageFive - 3.0F) > 0.001F || Math.abs(damageSix - 3.5F) > 0.001F) {
            helper.fail("Unexpected Sharpness scaling: V=" + damageFive + " VI=" + damageSix);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public void protectionAboveVanillaMaxIncreasesRealProtection(GameTestHelper helper) {
        Holder.Reference<Enchantment> protection = enchantment(helper, "protection");
        Player player = helper.makeMockPlayer();
        var source = helper.getLevel().damageSources().generic();

        ItemStack protFour = enchanted(new ItemStack(Items.DIAMOND_CHESTPLATE), protection, 4);
        player.setItemSlot(EquipmentSlot.CHEST, protFour);
        float protectionFour = EnchantmentHelper.getDamageProtection(helper.getLevel(), player, source);

        ItemStack protFive = enchanted(new ItemStack(Items.DIAMOND_CHESTPLATE), protection, 5);
        player.setItemSlot(EquipmentSlot.CHEST, protFive);
        float protectionFive = EnchantmentHelper.getDamageProtection(helper.getLevel(), player, source);

        if (!(protectionFive > protectionFour)) {
            helper.fail("Protection V must produce more real protection than Protection IV: IV=" + protectionFour + " V=" + protectionFive);
            return;
        }
        if (Math.abs(protectionFour - 4.0F) > 0.001F || Math.abs(protectionFive - 5.0F) > 0.001F) {
            helper.fail("Unexpected Protection scaling: IV=" + protectionFour + " V=" + protectionFive);
            return;
        }
        helper.succeed();
    }
}
