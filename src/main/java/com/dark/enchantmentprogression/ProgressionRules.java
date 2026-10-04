package com.dark.enchantmentprogression;

public final class ProgressionRules {
    public static final int MAX_LEVEL = 255;
    private ProgressionRules() {}
    public static int nextLevel(int current) { return Math.min(MAX_LEVEL, Math.max(0, current) + 1); }
    public static boolean canAdvance(int current) { return current >= 0 && current < MAX_LEVEL; }
    public static int xpCost(int currentLevel) { return UpgradeCost.levelsFor(nextLevel(currentLevel), 1.0); }
}
