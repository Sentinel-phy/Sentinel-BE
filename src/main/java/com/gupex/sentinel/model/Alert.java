package com.gupex.sentinel.model;

import java.time.Instant;

public class Alert {
    private String id;
    private String sessionId;
    private AlertLevel level;
    private String category;
    private String message;
    private boolean acknowledged;
    private Instant timestamp;

    public enum AlertLevel {
        INFO, WARNING, CRITICAL
    }

    public Alert() {}

    public Alert(String id, String sessionId, AlertLevel level, String category, String message) {
        this.id = id;
        this.sessionId = sessionId;
        this.level = level;
        this.category = category;
        this.message = message;
        this.acknowledged = false;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public AlertLevel getLevel() { return level; }
    public void setLevel(AlertLevel level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
