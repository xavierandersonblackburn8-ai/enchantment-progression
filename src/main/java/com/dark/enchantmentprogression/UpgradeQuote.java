package com.dark.enchantmentprogression;

public record UpgradeQuote(int currentLevel, int targetLevel, int xpCost) {
    public UpgradeQuote {
        if (currentLevel < 0) throw new IllegalArgumentException("currentLevel < 0");
        if (targetLevel != ProgressionRules.nextLevel(currentLevel)) throw new IllegalArgumentException("invalid target");
        if (xpCost != ProgressionRules.xpCost(currentLevel)) throw new IllegalArgumentException("invalid cost");
    }
    public static UpgradeQuote forCurrentLevel(int currentLevel) { return new UpgradeQuote(currentLevel, ProgressionRules.nextLevel(currentLevel), ProgressionRules.xpCost(currentLevel)); }
}
