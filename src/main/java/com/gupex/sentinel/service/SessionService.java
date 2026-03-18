package com.gupex.sentinel.service;

import com.gupex.sentinel.config.ServerConfig;
import com.gupex.sentinel.model.GpuInstance;
import com.gupex.sentinel.model.Session;
import com.gupex.sentinel.model.SessionStatus;
import com.gupex.sentinel.store.GpuPoolStore;
import com.gupex.sentinel.store.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionStore sessionStore;
    private final GpuPoolStore gpuPoolStore;
    private final BillingService billingService;
    private int sessionCounter = 0;

    public SessionService(SessionStore sessionStore, GpuPoolStore gpuPoolStore, BillingService billingService) {
        this.sessionStore = sessionStore;
        this.gpuPoolStore = gpuPoolStore;
        this.billingService = billingService;
    }

    public Session createSession(String userId, int durationHours, String environmentUsd, String forgeProjectId) {
        if (durationHours < 1 || durationHours > ServerConfig.getSessionMaxHours()) {
            throw new IllegalArgumentException("Duration must be between 1 and " + ServerConfig.getSessionMaxHours() + " hours");
        }

        String sessionId = "sess_" + String.format("%04d", ++sessionCounter);
        Session session = new Session(sessionId, userId, durationHours, environmentUsd, forgeProjectId);

        long estimatedCost = billingService.estimateCost(durationHours);
        session.setEstimatedCostKrw(estimatedCost);

        Optional<GpuInstance> gpu = gpuPoolStore.allocate(sessionId);
        if (gpu.isPresent()) {
            GpuInstance instance = gpu.get();
            session.setGpuInstanceId(instance.getId());
            session.setStatus(SessionStatus.INITIALIZING);
            session.setWebrtcUrl("wss://sentinel.talos.ai/stream/" + sessionId);
            log.info("Session {} allocated GPU {} ({})", sessionId, instance.getId(), instance.getGpuModel());

            // Simulate initialization delay
            Thread.startVirtualThread(() -> {
                try {
                    Thread.sleep(2000); // Simulate USD loading
                    session.setStatus(SessionStatus.RUNNING);
                    session.setStartedAt(Instant.now());
                    sessionStore.save(session);
                    log.info("Session {} is now RUNNING", sessionId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } else {
            session.setStatus(SessionStatus.PENDING);
            log.warn("Session {} queued - no GPU available", sessionId);
        }

        sessionStore.save(session);
        return session;
    }

    public Optional<Session> getSession(String sessionId) {
        return sessionStore.findById(sessionId);
    }

    public List<Session> getUserSessions(String userId) {
        return sessionStore.findByUserId(userId);
    }

    public List<Session> getAllSessions() {
        return sessionStore.findAll();
    }

    public List<Session> getActiveSessions() {
        return sessionStore.findActive();
    }

    public Session pauseSession(String sessionId) {
        Session session = sessionStore.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.RUNNING) {
            throw new IllegalStateException("Can only pause RUNNING sessions");
        }

        session.setStatus(SessionStatus.PAUSED);
        sessionStore.save(session);
        log.info("Session {} paused", sessionId);
        return session;
    }

    public Session resumeSession(String sessionId) {
        Session session = sessionStore.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.PAUSED) {
            throw new IllegalStateException("Can only resume PAUSED sessions");
        }

        session.setStatus(SessionStatus.RUNNING);
        sessionStore.save(session);
        log.info("Session {} resumed", sessionId);
        return session;
    }

    public Session terminateSession(String sessionId) {
        Session session = sessionStore.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));

        if (!session.isActive()) {
            throw new IllegalStateException("Session is not active");
        }

        session.setStatus(SessionStatus.TERMINATING);
        sessionStore.save(session);

        // Release GPU
        if (session.getGpuInstanceId() != null) {
            gpuPoolStore.release(session.getGpuInstanceId());
        }

        // Calculate actual cost
        if (session.getStartedAt() != null) {
            long durationSec = Instant.now().getEpochSecond() - session.getStartedAt().getEpochSecond();
            session.setTotalGpuSeconds(durationSec);
            double hours = durationSec / 3600.0;
            session.setActualCostKrw(billingService.calculateCost(hours));
        }

        session.setStatus(SessionStatus.TERMINATED);
        session.setTerminatedAt(Instant.now());
        sessionStore.save(session);
        log.info("Session {} terminated. Cost: ₩{}", sessionId, session.getActualCostKrw());
        return session;
    }

    public Map<String, Object> getSessionStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSessions", sessionStore.findAll().size());
        stats.put("activeSessions", sessionStore.countActive());
        stats.put("pendingSessions", sessionStore.countByStatus(SessionStatus.PENDING));
        stats.put("terminatedSessions", sessionStore.countByStatus(SessionStatus.TERMINATED));
        stats.put("gpuTotal", gpuPoolStore.countTotal());
        stats.put("gpuAvailable", gpuPoolStore.countAvailable());
        stats.put("gpuAllocated", gpuPoolStore.countAllocated());
        stats.put("gpuAvgUtilization", Math.round(gpuPoolStore.getAverageUtilization() * 100.0) / 100.0);
        return stats;
    }
}
