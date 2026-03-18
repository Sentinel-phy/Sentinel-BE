package com.gupex.sentinel.service;

import com.gupex.sentinel.config.ServerConfig;
import com.gupex.sentinel.model.Sim2RealComparison;
import com.gupex.sentinel.store.MonitoringStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);

    private final MonitoringStore monitoringStore;
    private final AlertService alertService;
    private final AtomicInteger comparisonCounter = new AtomicInteger(0);

    public MonitoringService(MonitoringStore monitoringStore, AlertService alertService) {
        this.monitoringStore = monitoringStore;
        this.alertService = alertService;
    }

    public Sim2RealComparison submitComparison(String sessionId, String forgeProjectId,
                                                double predictedThroughput, double predictedGapPercent,
                                                double predictionConfidence,
                                                double actualThroughput, double actualGapPercent) {
        String compId = "cmp_" + String.format("%04d", comparisonCounter.incrementAndGet());
        Sim2RealComparison comparison = new Sim2RealComparison(compId, sessionId);
        comparison.setForgeProjectId(forgeProjectId);
        comparison.setPredictedThroughput(predictedThroughput);
        comparison.setPredictedGapPercent(predictedGapPercent);
        comparison.setPredictionConfidence(predictionConfidence);
        comparison.setActualThroughput(actualThroughput);
        comparison.setActualGapPercent(actualGapPercent);
        comparison.computeDrift();

        monitoringStore.saveComparison(comparison);
        log.info("Sim2Real comparison {}: drift={}% status={}",
                compId, comparison.getDriftPercent(), comparison.getDriftStatus());

        // Check drift threshold
        if (comparison.getDriftPercent() > ServerConfig.getSim2RealDriftThreshold()) {
            alertService.checkSim2RealDrift(sessionId, comparison.getDriftPercent());
        }

        return comparison;
    }

    public List<Sim2RealComparison> getSessionComparisons(String sessionId) {
        return monitoringStore.findComparisonsBySessionId(sessionId);
    }

    public List<Sim2RealComparison> getRecentComparisons(int limit) {
        return monitoringStore.getRecentComparisons(limit);
    }

    public Map<String, Object> getDriftSummary() {
        List<Sim2RealComparison> recent = monitoringStore.getRecentComparisons(50);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalComparisons", recent.size());

        if (!recent.isEmpty()) {
            double avgDrift = recent.stream().mapToDouble(Sim2RealComparison::getDriftPercent).average().orElse(0);
            double maxDrift = recent.stream().mapToDouble(Sim2RealComparison::getDriftPercent).max().orElse(0);
            long normalCount = recent.stream().filter(c -> "NORMAL".equals(c.getDriftStatus())).count();
            long warningCount = recent.stream().filter(c -> "WARNING".equals(c.getDriftStatus())).count();
            long criticalCount = recent.stream().filter(c -> "CRITICAL".equals(c.getDriftStatus())).count();

            summary.put("avgDriftPercent", Math.round(avgDrift * 100.0) / 100.0);
            summary.put("maxDriftPercent", Math.round(maxDrift * 100.0) / 100.0);
            summary.put("normalCount", normalCount);
            summary.put("warningCount", warningCount);
            summary.put("criticalCount", criticalCount);
        }
        return summary;
    }

    /**
     * Generate demo Sim2Real comparisons for PoC demonstration.
     */
    public void generateDemoData() {
        Random random = new Random();
        String[] sessions = {"sess_0001", "sess_0002", "sess_0003"};
        String[] projects = {"proj_warehouse", "proj_assembly", "proj_logistics"};

        for (int i = 0; i < sessions.length; i++) {
            for (int j = 0; j < 5; j++) {
                double predicted = 40 + random.nextDouble() * 20;
                double predictedGap = -(10 + random.nextDouble() * 10);
                double confidence = 0.75 + random.nextDouble() * 0.2;
                double actual = predicted * (0.85 + random.nextDouble() * 0.3);
                double actualGap = predictedGap + (random.nextDouble() - 0.5) * 6;

                submitComparison(sessions[i], projects[i],
                        predicted, predictedGap, confidence, actual, actualGap);
            }
        }
        log.info("Generated demo Sim2Real data");
    }
}
