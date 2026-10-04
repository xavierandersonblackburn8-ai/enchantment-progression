package com.dark.enchantmentprogression;

public final class UpgradeCost {
    private UpgradeCost() {}
    public static int levelsFor(int targetLevel, double weight) {
        if (targetLevel < 1) throw new IllegalArgumentException("targetLevel < 1");
        if (!Double.isFinite(weight) || weight <= 0.0) throw new IllegalArgumentException("weight must be finite and > 0");
        long base = switch (targetLevel) {
            case 1 -> 5L; case 2 -> 10L; case 3 -> 18L; case 4 -> 24L;
            case 5 -> 30L; case 6 -> 45L; case 7 -> 65L; case 8 -> 90L;
            case 9 -> 120L; case 10 -> 160L;
            default -> { long d = targetLevel - 10L; yield 160L + d * 50L + (d * (d - 1L) / 2L) * 5L; }
        };
        double weighted = base * weight;
        if (weighted >= 10000.0) return 10000;
        return Math.max(1, (int)Math.round(weighted));
    }
}
