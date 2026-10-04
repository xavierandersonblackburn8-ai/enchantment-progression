package com.dark.enchantmentprogression;

public final class UpgradePolicy {
    private UpgradePolicy() {}
    public static boolean canAfford(boolean creativeInstabuild, int experienceLevel, int cost) { return creativeInstabuild || experienceLevel >= cost; }
    public static boolean shouldCharge(boolean creativeInstabuild, int requestedLevel, int appliedLevel) { return !creativeInstabuild && requestedLevel == appliedLevel; }
}
