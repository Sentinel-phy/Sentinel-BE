package com.gupex.sentinel.model;

import java.time.Instant;

public class GpuInstance {
    private String id;
    private String name;
    private String gpuModel;
    private int vramGb;
    private String provider;
    private double hourlyRateUsd;
    private GpuInstanceStatus status;
    private double gpuUtilization;
    private double vramUsedGb;
    private String currentSessionId;
    private Instant createdAt;

    public enum GpuInstanceStatus {
        AVAILABLE, ALLOCATED, MAINTENANCE, OFFLINE
    }

    public GpuInstance() {}

    public GpuInstance(String id, String name, String gpuModel, int vramGb, String provider, double hourlyRateUsd) {
        this.id = id;
        this.name = name;
        this.gpuModel = gpuModel;
        this.vramGb = vramGb;
        this.provider = provider;
        this.hourlyRateUsd = hourlyRateUsd;
        this.status = GpuInstanceStatus.AVAILABLE;
        this.gpuUtilization = 0.0;
        this.vramUsedGb = 0.0;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGpuModel() { return gpuModel; }
    public void setGpuModel(String gpuModel) { this.gpuModel = gpuModel; }

    public int getVramGb() { return vramGb; }
    public void setVramGb(int vramGb) { this.vramGb = vramGb; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public double getHourlyRateUsd() { return hourlyRateUsd; }
    public void setHourlyRateUsd(double hourlyRateUsd) { this.hourlyRateUsd = hourlyRateUsd; }

    public GpuInstanceStatus getStatus() { return status; }
    public void setStatus(GpuInstanceStatus status) { this.status = status; }

    public double getGpuUtilization() { return gpuUtilization; }
    public void setGpuUtilization(double gpuUtilization) { this.gpuUtilization = gpuUtilization; }

    public double getVramUsedGb() { return vramUsedGb; }
    public void setVramUsedGb(double vramUsedGb) { this.vramUsedGb = vramUsedGb; }

    public String getCurrentSessionId() { return currentSessionId; }
    public void setCurrentSessionId(String currentSessionId) { this.currentSessionId = currentSessionId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isAvailable() {
        return status == GpuInstanceStatus.AVAILABLE && currentSessionId == null;
    }
}
