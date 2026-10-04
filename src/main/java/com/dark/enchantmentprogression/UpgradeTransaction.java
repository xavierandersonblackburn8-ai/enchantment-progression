package com.dark.enchantmentprogression;

public final class UpgradeTransaction {
    private UpgradeTransaction() {}
    public enum Outcome { SUCCESS_CHARGE, SUCCESS_FREE, REJECT_UNAFFORDABLE, REJECT_WRITE }
    public static Outcome decide(boolean creative, int xpLevels, int cost, int requestedLevel, int storedLevel) {
        if (!UpgradePolicy.canAfford(creative, xpLevels, cost)) return Outcome.REJECT_UNAFFORDABLE;
        if (storedLevel != requestedLevel) return Outcome.REJECT_WRITE;
        return UpgradePolicy.shouldCharge(creative, requestedLevel, storedLevel) ? Outcome.SUCCESS_CHARGE : Outcome.SUCCESS_FREE;
    }
}
