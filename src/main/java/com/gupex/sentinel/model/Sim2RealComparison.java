package com.gupex.sentinel.model;

import java.time.Instant;

public class Sim2RealComparison {
    private String id;
    private String sessionId;
    private String forgeProjectId;

    // LSTM prediction
    private double predictedThroughput;
    private double predictedGapPercent;
    private double predictionConfidence;

    // Actual performance
    private double actualThroughput;
    private double actualGapPercent;

    // Drift analysis
    private double driftPercent;
    private String driftStatus; // NORMAL, WARNING, CRITICAL

    private Instant timestamp;

    public Sim2RealComparison() {}

    public Sim2RealComparison(String id, String sessionId) {
        this.id = id;
        this.sessionId = sessionId;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getForgeProjectId() { return forgeProjectId; }
    public void setForgeProjectId(String forgeProjectId) { this.forgeProjectId = forgeProjectId; }

    public double getPredictedThroughput() { return predictedThroughput; }
    public void setPredictedThroughput(double predictedThroughput) { this.predictedThroughput = predictedThroughput; }

    public double getPredictedGapPercent() { return predictedGapPercent; }
    public void setPredictedGapPercent(double predictedGapPercent) { this.predictedGapPercent = predictedGapPercent; }

    public double getPredictionConfidence() { return predictionConfidence; }
    public void setPredictionConfidence(double predictionConfidence) { this.predictionConfidence = predictionConfidence; }

    public double getActualThroughput() { return actualThroughput; }
    public void setActualThroughput(double actualThroughput) { this.actualThroughput = actualThroughput; }

    public double getActualGapPercent() { return actualGapPercent; }
    public void setActualGapPercent(double actualGapPercent) { this.actualGapPercent = actualGapPercent; }

    public double getDriftPercent() { return driftPercent; }
    public void setDriftPercent(double driftPercent) { this.driftPercent = driftPercent; }

    public String getDriftStatus() { return driftStatus; }
    public void setDriftStatus(String driftStatus) { this.driftStatus = driftStatus; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public void computeDrift() {
        this.driftPercent = Math.abs(predictedGapPercent - actualGapPercent);
        if (driftPercent <= 2.0) {
            this.driftStatus = "NORMAL";
        } else if (driftPercent <= 5.0) {
            this.driftStatus = "WARNING";
        } else {
            this.driftStatus = "CRITICAL";
        }
    }
}
