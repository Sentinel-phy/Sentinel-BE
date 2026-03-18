package com.gupex.sentinel.model;

import java.time.Instant;

public class MeteringRecord {
    private String id;
    private String sessionId;
    private String gpuInstanceId;
    private double gpuUtilization;
    private double vramUsedGb;
    private double gpuTempCelsius;
    private double powerWatts;
    private Instant timestamp;

    public MeteringRecord() {}

    public MeteringRecord(String id, String sessionId, String gpuInstanceId,
                          double gpuUtilization, double vramUsedGb,
                          double gpuTempCelsius, double powerWatts) {
        this.id = id;
        this.sessionId = sessionId;
        this.gpuInstanceId = gpuInstanceId;
        this.gpuUtilization = gpuUtilization;
        this.vramUsedGb = vramUsedGb;
        this.gpuTempCelsius = gpuTempCelsius;
        this.powerWatts = powerWatts;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getGpuInstanceId() { return gpuInstanceId; }
    public void setGpuInstanceId(String gpuInstanceId) { this.gpuInstanceId = gpuInstanceId; }

    public double getGpuUtilization() { return gpuUtilization; }
    public void setGpuUtilization(double gpuUtilization) { this.gpuUtilization = gpuUtilization; }

    public double getVramUsedGb() { return vramUsedGb; }
    public void setVramUsedGb(double vramUsedGb) { this.vramUsedGb = vramUsedGb; }

    public double getGpuTempCelsius() { return gpuTempCelsius; }
    public void setGpuTempCelsius(double gpuTempCelsius) { this.gpuTempCelsius = gpuTempCelsius; }

    public double getPowerWatts() { return powerWatts; }
    public void setPowerWatts(double powerWatts) { this.powerWatts = powerWatts; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
