package com.gupex.sentinel.model;

public enum SessionStatus {
    PENDING,
    INITIALIZING,
    RUNNING,
    PAUSED,
    TERMINATING,
    TERMINATED,
    FAILED
}
