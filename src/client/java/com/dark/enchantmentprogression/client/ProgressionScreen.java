package com.dark.enchantmentprogression.client;

import com.dark.enchantmentprogression.ClientUpgradeEligibility;
import com.dark.enchantmentprogression.ProgressionDisplay;
import com.dark.enchantmentprogression.PendingUpgrade;
import com.dark.enchantmentprogression.UpgradeSession;
import com.dark.enchantmentprogression.network.UpgradeRequest;
import com.dark.enchantmentprogression.network.UpgradeResult;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.ArrayList;
import java.util.List;

public final class ProgressionScreen extends Screen {
    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    private int scroll;
    private final PendingUpgrade pendingUpgrade = new PendingUpgrade();
    private final UpgradeSession upgradeSession = new UpgradeSession();
    private int refreshTicks;
    private Component status = Component.empty();
    private int statusColor = 0xFFAAAAAA;

    public ProgressionScreen(Screen parent) { super(Component.translatable("enchantment_progression.title")); this.parent = parent; }

    @Override protected void init() {
        super.init();
        rebuild();
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose()).bounds(width / 2 - 45, height - 30, 90, 20).build());
    }

    private void rebuild() {
        rows.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) return;
        var registry = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        registry.listElements().forEach(holder -> {
            if (!holder.value().isSupportedItem(stack)) return;
            int level = EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
            boolean compatible = true;
            var stored = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            for (var entry : stored.entrySet()) {
                if (entry.getIntValue() > 0 && !entry.getKey().equals(holder) && !Enchantment.areCompatible(entry.getKey(), holder)) { compatible = false; break; }
            }
            rows.add(new Row(holder, level, compatible));
        });
        rows.sort((a,b) -> a.name().getString().compareToIgnoreCase(b.name().getString()));
        scroll = Math.min(scroll, Math.max(0, rows.size() - 7));
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int left = width / 2 - 150, top = 28;
        g.fill(left, top, left + 300, height - 38, 0xE5141519);
        g.fill(left, top, left + 300, top + 2, 0xFFFF2424);
        g.centeredText(font, title, width / 2, top + 10, 0xFFFFFFFF);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack stack = mc.player.getMainHandItem();
            g.fakeItem(stack, left + 12, top + 26);
            g.text(font, stack.isEmpty() ? Component.translatable("enchantment_progression.no_item") : stack.getHoverName(), left + 36, top + 32, 0xFFFFFFFF, false);
            g.text(font, Component.translatable("enchantment_progression.xp", mc.player.experienceLevel), left + 210, top + 32, 0xFF55FF55, false);
        }
        int y = top + 55, shown = 0;
        for (int i = scroll; i < rows.size() && shown < 7; i++, shown++) {
            Row r = rows.get(i);
            ProgressionDisplay display = ProgressionDisplay.forLevel(r.level);
            int target = display.targetLevel();
            int cost = display.xpCost();
            g.fill(left + 10, y, left + 290, y + 24, 0xCC24262D);
            g.text(font, r.name(), left + 16, y + 5, 0xFFFFFFFF, false);
            g.text(font, Component.literal(roman(r.level) + " → " + roman(target)), left + 142, y + 5, 0xFF55FFFF, false);
            boolean affordable = mc.player != null && (mc.player.getAbilities().instabuild || mc.player.experienceLevel >= cost);
            int xpColor = (!r.compatible || display.capped()) ? 0xFF777777 : (affordable ? 0xFF55FF55 : 0xFFFF6666);
            g.text(font, Component.literal(display.capped() ? "MAX" : cost + " XP"), left + 205, y + 5, xpColor, false);
            g.text(font, Component.literal(display.capped() ? "MAX" : (r.compatible ? "UPGRADE" : "LOCKED")), left + 252, y + 5, xpColor, false);
            if (mouseX >= left + 10 && mouseX <= left + 290 && mouseY >= y && mouseY <= y + 24) g.outline(left + 10, y, 280, 24, 0xFFFF3333);
            y += 28;
        }
        if (!status.getString().isEmpty()) g.centeredText(font, status, width / 2, height - 48, statusColor);
        if (pendingUpgrade.isPending()) g.centeredText(font, Component.literal("Upgrading…"), width / 2, top + 43, 0xFFFFCC55);
        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int left = width / 2 - 150, y = 83;
        for (int i = scroll, shown = 0; i < rows.size() && shown < 7; i++, shown++, y += 28) {
            Row row = rows.get(i);
            Minecraft mc = Minecraft.getInstance();
            boolean creative = mc.player != null && mc.player.getAbilities().instabuild;
            int xp = mc.player == null ? 0 : mc.player.experienceLevel;
            if (ClientUpgradeEligibility.canRequest(pendingUpgrade.isPending(), row.compatible, creative, row.level, xp) && mx >= left + 10 && mx <= left + 290 && my >= y && my <= y + 24) {
                Identifier id = row.holder.unwrapKey().orElseThrow().identifier();
                long requestId = upgradeSession.begin(id.toString());
                if (requestId == 0L) return true;
                pendingUpgrade.begin();
                status = Component.empty();
                if (!ClientPlayNetworking.canSend(UpgradeRequest.TYPE)) {
                    upgradeSession.complete(); pendingUpgrade.complete();
                    status = Component.literal("Server does not support Enchantment Progression"); statusColor = 0xFFFF6666; return true;
                }
                ClientPlayNetworking.send(new UpgradeRequest(requestId, id));
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scroll = Math.max(0, Math.min(Math.max(0, rows.size() - 7), scroll + (vertical < 0 ? 1 : -1)));
        return true;
    }

    public void acceptResult(UpgradeResult result) {
        if (!upgradeSession.accepts(result.requestId(), result.enchantmentId().toString())) return;
        upgradeSession.complete(); pendingUpgrade.complete();
        if (result.success()) {
            status = Component.literal("Upgrade complete — level " + result.newLevel() + " (-" + result.xpCost() + " XP, " + result.remainingXpLevels() + " remaining)"); statusColor = 0xFF55FF55;
        } else {
            status = Component.literal(switch (result.reason()) {
                case "not_enough_xp" -> "Not enough XP levels";
                case "incompatible" -> "That enchantment conflicts with one already on the item";
                case "not_supported" -> "That enchantment cannot be applied to this item";
                case "unknown_enchantment" -> "That enchantment no longer exists in this world";
                case "level_cap" -> "Configured progression cap reached";
                case "no_item" -> "Hold the item you want to upgrade in your main hand";
                case "rate_limited" -> "Upgrade request was too fast — try again";
                case "not_enchantable" -> "That item cannot store enchantments";
                case "write_rejected" -> "Minecraft rejected that level; no XP was charged";
                default -> "Upgrade rejected: " + result.reason();
            }); statusColor = 0xFFFF6666;
        }
        rebuild();
    }

    @Override public void tick() {
        super.tick();
        if (++refreshTicks >= 10) { refreshTicks = 0; rebuild(); }
        if (pendingUpgrade.tick()) { upgradeSession.complete(); status = Component.literal("Upgrade timed out — no confirmation received"); statusColor = 0xFFFF6666; rebuild(); }
    }
    @Override public void onClose() { Minecraft.getInstance().gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
    private record Row(Holder.Reference<Enchantment> holder, int level, boolean compatible) { Component name() { return holder.value().description().copy(); } }
    private static String roman(int n) {
        if (n <= 0) return "—"; if (n > 20) return Integer.toString(n);
        int[] v = {10, 9, 5, 4, 1}; String[] s = {"X", "IX", "V", "IV", "I"}; StringBuilder b = new StringBuilder();
        for (int i = 0; i < v.length; i++) while (n >= v[i]) { b.append(s[i]); n -= v[i]; }
        return b.toString();
    }
}
