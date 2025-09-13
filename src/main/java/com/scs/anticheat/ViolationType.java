package com.scs.anticheat;

public enum ViolationType {
    MOVE(1, 0x55FF55, "Move", "Подозрительное движение"),
    FASTPLACE(2, 0xFFAA00, "FastPlace", "Быстрая установка блоков"),
    FASTBREAK(2, 0xFFAA00, "FastBreak", "Быстрое разрушение блоков"),
    DELAY(2, 0xFFAA00, "Delay", "Задержка пакетов"),
    CLICK(3, 0xFF5555, "Click", "Подозрительные клики"),
    HITBOX(4, 0xFF0000, "HitBox", "Увеличенный хитбокс"),
    VELOCITY(4, 0xFF0000, "Velocity", "Игнорирование откидывания"),
    KILLAURA(5, 0xAA0000, "KillAura", "Автоматическая атака"),
    AUTOBOT(5, 0x800080, "AutoBot", "Автоматическое поведение");

    private final int priority;
    private final int color;
    private final String displayName;
    private final String description;

    ViolationType(int priority, int color, String displayName, String description) {
        this.priority = priority;
        this.color = color;
        this.displayName = displayName;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public int getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Определяет, является ли нарушение критичным
     * @return true если приоритет >= 4 (серьезные читы)
     */
    public boolean isCritical() {
        return priority >= 4;
    }

    /**
     * Получает цвет в формате Minecraft
     * @return строка цвета для Minecraft чата
     */
    public String getMinecraftColor() {
        switch (this) {
            case MOVE:
                return "§a"; // Зеленый
            case FASTPLACE:
            case FASTBREAK:
            case DELAY:
                return "§e"; // Желтый
            case CLICK:
                return "§6"; // Оранжевый
            case HITBOX:
            case VELOCITY:
                return "§c"; // Красный
            case KILLAURA:
            case AUTOBOT:
                return "§4"; // Темно-красный
            default:
                return "§f"; // Белый
        }
    }

    /**
     * Получает эмодзи для типа нарушения
     * @return эмодзи соответствующее типу
     */
    public String getEmoji() {
        switch (this) {
            case MOVE:
                return "🏃";
            case FASTPLACE:
            case FASTBREAK:
                return "⚡";
            case DELAY:
                return "⏱️";
            case CLICK:
                return "🖱️";
            case HITBOX:
                return "📏";
            case VELOCITY:
                return "💥";
            case KILLAURA:
                return "⚔️";
            case AUTOBOT:
                return "🤖";
            default:
                return "❓";
        }
    }

    /**
     * Находит тип нарушения по ключевым словам в сообщении
     * @param message текст сообщения
     * @return соответствующий тип нарушения или MOVE по умолчанию
     */
    public static ViolationType fromMessage(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("move") || lowerMessage.contains("abnormally")) {
            return MOVE;
        }
        if (lowerMessage.contains("fastplace")) {
            return FASTPLACE;
        }
        if (lowerMessage.contains("fastbreak")) {
            return FASTBREAK;
        }
        if (lowerMessage.contains("delay")) {
            return DELAY;
        }
        if (lowerMessage.contains("click")) {
            return CLICK;
        }
        if (lowerMessage.contains("hitbox") || lowerMessage.contains("reach")) {
            return HITBOX;
        }
        if (lowerMessage.contains("velocity")) {
            return VELOCITY;
        }
        if (lowerMessage.contains("killaura") || lowerMessage.contains("combat hacks")) {
            return KILLAURA;
        }
        if (lowerMessage.contains("autobot") || lowerMessage.contains("automatic robots")) {
            return AUTOBOT;
        }

        return MOVE; // По умолчанию
    }

    /**
     * Получает рекомендуемое действие для типа нарушения
     * @return строка с рекомендуемым действием
     */
    public String getRecommendedAction() {
        switch (this) {
            case MOVE:
            case FASTPLACE:
            case FASTBREAK:
            case DELAY:
                return "Наблюдение";
            case CLICK:
            case HITBOX:
                return "Проверка";
            case VELOCITY:
                return "Заморозка";
            case KILLAURA:
            case AUTOBOT:
                return "Немедленная проверка";
            default:
                return "Стандартная проверка";
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}