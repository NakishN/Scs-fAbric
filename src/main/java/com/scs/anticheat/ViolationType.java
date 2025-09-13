package com.scs.anticheat;

public enum ViolationType {
    MOVE(1, 0x55FF55),
    FASTPLACE(2, 0xFFAA00),
    FASTBREAK(2, 0xFFAA00),
    DELAY(2, 0xFFAA00),
    CLICK(3, 0xFF5555),
    HITBOX(4, 0xFF0000),
    VELOCITY(4, 0xFF0000),
    KILLAURA(5, 0xAA0000),
    AUTOBOT(5, 0x800080);

    private final int priority;
    private final int color;

    ViolationType(int priority, int color) {
        this.priority = priority;
        this.color = color;
    }

    public int getPriority() {
        return priority;
    }

    public int getColor() {
        return color;
    }

    public boolean isCritical() {
        return priority >= 4;
    }
}