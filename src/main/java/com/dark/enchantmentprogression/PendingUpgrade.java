package com.dark.enchantmentprogression;

/**
 * Small client-side state machine for one in-flight upgrade request.
 * It prevents duplicate purchases and guarantees the UI cannot remain locked forever
 * if a connection closes or a reply is lost while the screen is open.
 */
public final class PendingUpgrade {
    public static final int DEFAULT_TIMEOUT_TICKS = 100;
    private int ticksRemaining;
    public boolean isPending() { return ticksRemaining > 0; }
    public void begin() { begin(DEFAULT_TIMEOUT_TICKS); }
    void begin(int timeoutTicks) {
        if (timeoutTicks <= 0) throw new IllegalArgumentException("timeoutTicks must be positive");
        ticksRemaining = timeoutTicks;
    }
    public boolean tick() {
        if (ticksRemaining <= 0) return false;
        ticksRemaining--;
        return ticksRemaining == 0;
    }
    public void complete() { ticksRemaining = 0; }
}
