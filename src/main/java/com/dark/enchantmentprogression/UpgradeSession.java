package com.dark.enchantmentprogression;

public final class UpgradeSession {
    private long nextRequestId = 1L;
    private long requestedId;
    private String requestedEnchantment;
    public long begin(String enchantmentId) {
        if (requestedEnchantment != null || enchantmentId == null || enchantmentId.isBlank()) return 0L;
        long id = nextRequestId++;
        if (id <= 0L) { nextRequestId = 2L; id = 1L; }
        requestedId = id; requestedEnchantment = enchantmentId; return id;
    }
    public boolean accepts(long requestId, String enchantmentId) { return requestedEnchantment != null && requestedId == requestId && requestedEnchantment.equals(enchantmentId); }
    public void complete() { requestedId = 0L; requestedEnchantment = null; }
    public boolean isPending() { return requestedEnchantment != null; }
}
