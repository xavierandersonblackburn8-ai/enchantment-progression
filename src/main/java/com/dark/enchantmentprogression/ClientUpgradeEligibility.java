package com.dark.enchantmentprogression;

/**
 * Pure client-side preflight for whether an upgrade row should be clickable.
 * The server still repeats every authoritative check; this only prevents
 * obviously invalid packets and keeps the UI state consistent with its labels.
 */
public final class ClientUpgradeEligibility {
    private ClientUpgradeEligibility() {}

    public static boolean canRequest(boolean pending, boolean compatible, boolean creative,
                                     int currentLevel, int playerXpLevels) {
        if (pending || !compatible || !ProgressionRules.canAdvance(currentLevel)) return false;
        int cost = ProgressionRules.xpCost(currentLevel);
        return creative || playerXpLevels >= cost;
    }
}
