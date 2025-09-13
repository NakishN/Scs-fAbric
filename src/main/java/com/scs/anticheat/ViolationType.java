package com.scs.anticheat;

public enum ViolationType {
    MOVE(1, 0x55FF55),          // Зеленый - низкий приоритет
    FASTPLACE(2, 0xFFAA00),     // Оранжевый
    FASTBREAK(2, 0xFFAA00),     // Оранжевый
    DELAY(2, 0xFFAA00),         // Оранжевый
    CLICK(3, 0xFF5555),         // Красный
    HITBOX(4, 0xFF0000),        // Ярко-красный - важное
    VELOCITY(4, 0xFF0000),      // Ярко-красный - важное
    KILLAURA(5, 0xAA0000),      // Темно-красный - критично
    AUTOBOT(5, 0x800080);       // Фиолетовый - критично

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