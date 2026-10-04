package com.dark.enchantmentprogression;

public record ProgressionDisplay(int currentLevel, int targetLevel, int xpCost, boolean capped) {
    public static ProgressionDisplay forLevel(int currentLevel) {
        if (currentLevel < 0) throw new IllegalArgumentException("currentLevel < 0");
        if (!ProgressionRules.canAdvance(currentLevel)) return new ProgressionDisplay(currentLevel, currentLevel, 0, true);
        UpgradeQuote q = UpgradeQuote.forCurrentLevel(currentLevel);
        return new ProgressionDisplay(currentLevel, q.targetLevel(), q.xpCost(), false);
    }
}
