package com.scs.anticheat;

public class AntiCheatDetection {
    private final String playerName;
    private final String fullMessage;
    private final ViolationType type;
    private final int count;
    private final long timestamp;

    public AntiCheatDetection(String playerName, String fullMessage, ViolationType type, int count) {
        this.playerName = playerName;
        this.fullMessage = fullMessage;
        this.type = type;
        this.count = count;
        this.timestamp = System.currentTimeMillis();
    }

    public String getPlayerName() { return playerName; }
    public String getFullMessage() { return fullMessage; }
    public ViolationType getType() { return type; }
    public int getCount() { return count; }
    public long getTimestamp() { return timestamp; }
}