package com.gupex.sentinel.model;

import java.time.Instant;

public class Session {
    private String id;
    private String userId;
    private String gpuInstanceId;
    private String forgeProjectId;
    private String environmentUsd;
    private int durationHours;
    private SessionStatus status;
    private String webrtcUrl;
    private long estimatedCostKrw;
    private long actualCostKrw;
    private Instant createdAt;
    private Instant startedAt;
    private Instant terminatedAt;
    private double peakGpuUtilization;
    private double peakVramGb;
    private long totalGpuSeconds;

    public Session() {}

    public Session(String id, String userId, int durationHours, String environmentUsd, String forgeProjectId) {
        this.id = id;
        this.userId = userId;
        this.durationHours = durationHours;
        this.environmentUsd = environmentUsd;
        this.forgeProjectId = forgeProjectId;
        this.status = SessionStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getGpuInstanceId() { return gpuInstanceId; }
    public void setGpuInstanceId(String gpuInstanceId) { this.gpuInstanceId = gpuInstanceId; }

    public String getForgeProjectId() { return forgeProjectId; }
    public void setForgeProjectId(String forgeProjectId) { this.forgeProjectId = forgeProjectId; }

    public String getEnvironmentUsd() { return environmentUsd; }
    public void setEnvironmentUsd(String environmentUsd) { this.environmentUsd = environmentUsd; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public String getWebrtcUrl() { return webrtcUrl; }
    public void setWebrtcUrl(String webrtcUrl) { this.webrtcUrl = webrtcUrl; }

    public long getEstimatedCostKrw() { return estimatedCostKrw; }
    public void setEstimatedCostKrw(long estimatedCostKrw) { this.estimatedCostKrw = estimatedCostKrw; }

    public long getActualCostKrw() { return actualCostKrw; }
    public void setActualCostKrw(long actualCostKrw) { this.actualCostKrw = actualCostKrw; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getTerminatedAt() { return terminatedAt; }
    public void setTerminatedAt(Instant terminatedAt) { this.terminatedAt = terminatedAt; }

    public double getPeakGpuUtilization() { return peakGpuUtilization; }
    public void setPeakGpuUtilization(double peakGpuUtilization) { this.peakGpuUtilization = peakGpuUtilization; }

    public double getPeakVramGb() { return peakVramGb; }
    public void setPeakVramGb(double peakVramGb) { this.peakVramGb = peakVramGb; }

    public long getTotalGpuSeconds() { return totalGpuSeconds; }
    public void setTotalGpuSeconds(long totalGpuSeconds) { this.totalGpuSeconds = totalGpuSeconds; }

    public boolean isActive() {
        return status == SessionStatus.RUNNING || status == SessionStatus.INITIALIZING || status == SessionStatus.PAUSED;
    }
}
