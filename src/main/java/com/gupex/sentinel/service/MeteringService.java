package com.gupex.sentinel.service;

import com.gupex.sentinel.model.GpuInstance;
import com.gupex.sentinel.model.MeteringRecord;
import com.gupex.sentinel.model.Session;
import com.gupex.sentinel.store.GpuPoolStore;
import com.gupex.sentinel.store.MeteringStore;
import com.gupex.sentinel.store.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MeteringService {

    private static final Logger log = LoggerFactory.getLogger(MeteringService.class);

    private final MeteringStore meteringStore;
    private final SessionStore sessionStore;
    private final GpuPoolStore gpuPoolStore;
    private final AlertService alertService;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger recordCounter = new AtomicInteger(0);
    private final Random random = new Random();

    public MeteringService(MeteringStore meteringStore, SessionStore sessionStore,
                           GpuPoolStore gpuPoolStore, AlertService alertService) {
        this.meteringStore = meteringStore;
        this.sessionStore = sessionStore;
        this.gpuPoolStore = gpuPoolStore;
        this.alertService = alertService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "metering-collector");
            t.setDaemon(true);
            return t;
        });
    }

    public void startCollection(int intervalSec) {
        scheduler.scheduleAtFixedRate(this::collectMetrics, 5, intervalSec, TimeUnit.SECONDS);
        log.info("Metering collection started (interval: {}s)", intervalSec);
    }

    public void stopCollection() {
        scheduler.shutdown();
        log.info("Metering collection stopped");
    }

    private void collectMetrics() {
        try {
            List<Session> activeSessions = sessionStore.findActive();
            for (Session session : activeSessions) {
                if (session.getGpuInstanceId() == null) continue;

                GpuInstance gpu = gpuPoolStore.findById(session.getGpuInstanceId()).orElse(null);
                if (gpu == null) continue;

                // PoC: simulate GPU metrics
                double utilization = simulateUtilization(gpu);
                double vram = simulateVram(gpu);
                double temp = 45 + utilization * 0.4 + random.nextDouble() * 5;
                double power = 50 + utilization * 2.5;

                gpu.setGpuUtilization(utilization);
                gpu.setVramUsedGb(vram);

                String recordId = "mr_" + recordCounter.incrementAndGet();
                MeteringRecord record = new MeteringRecord(
                        recordId, session.getId(), gpu.getId(),
                        utilization, vram, temp, power
                );
                meteringStore.save(record);

                // Track peak values on session
                if (utilization > session.getPeakGpuUtilization()) {
                    session.setPeakGpuUtilization(utilization);
                }
                if (vram > session.getPeakVramGb()) {
                    session.setPeakVramGb(vram);
                }
                sessionStore.save(session);

                // Check alert conditions
                if (utilization > 95.0) {
                    alertService.checkGpuUtilization(session.getId(), utilization);
                }
            }
        } catch (Exception e) {
            log.error("Metering collection error", e);
        }
    }

    private double simulateUtilization(GpuInstance gpu) {
        // PoC: realistic GPU utilization pattern
        double base = 40 + random.nextDouble() * 40;
        double spike = random.nextDouble() < 0.1 ? random.nextDouble() * 30 : 0;
        return Math.min(100, base + spike);
    }

    private double simulateVram(GpuInstance gpu) {
        double maxVram = gpu.getVramGb();
        double base = maxVram * 0.3 + random.nextDouble() * maxVram * 0.4;
        return Math.round(base * 10.0) / 10.0;
    }

    public List<MeteringRecord> getSessionMetrics(String sessionId) {
        return meteringStore.findBySessionId(sessionId);
    }

    public List<MeteringRecord> getGpuMetrics(String gpuInstanceId) {
        return meteringStore.findByGpuInstanceId(gpuInstanceId);
    }

    public List<MeteringRecord> getRecentMetrics(int limit) {
        return meteringStore.getRecent(limit);
    }

    public Map<String, Object> getSessionMeteringSummary(String sessionId) {
        List<MeteringRecord> records = meteringStore.findBySessionId(sessionId);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("sessionId", sessionId);
        summary.put("totalRecords", records.size());

        if (!records.isEmpty()) {
            double avgUtil = records.stream().mapToDouble(MeteringRecord::getGpuUtilization).average().orElse(0);
            double maxUtil = records.stream().mapToDouble(MeteringRecord::getGpuUtilization).max().orElse(0);
            double avgVram = records.stream().mapToDouble(MeteringRecord::getVramUsedGb).average().orElse(0);
            double maxVram = records.stream().mapToDouble(MeteringRecord::getVramUsedGb).max().orElse(0);
            double avgTemp = records.stream().mapToDouble(MeteringRecord::getGpuTempCelsius).average().orElse(0);
            double avgPower = records.stream().mapToDouble(MeteringRecord::getPowerWatts).average().orElse(0);

            summary.put("avgGpuUtilization", Math.round(avgUtil * 100.0) / 100.0);
            summary.put("maxGpuUtilization", Math.round(maxUtil * 100.0) / 100.0);
            summary.put("avgVramGb", Math.round(avgVram * 100.0) / 100.0);
            summary.put("maxVramGb", Math.round(maxVram * 100.0) / 100.0);
            summary.put("avgTempCelsius", Math.round(avgTemp * 10.0) / 10.0);
            summary.put("avgPowerWatts", Math.round(avgPower * 10.0) / 10.0);
        }
        return summary;
    }

    public Map<String, Object> getOverallStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRecords", meteringStore.getTotalRecords());

        List<MeteringRecord> recent = meteringStore.getRecent(100);
        if (!recent.isEmpty()) {
            double avgUtil = recent.stream().mapToDouble(MeteringRecord::getGpuUtilization).average().orElse(0);
            double avgVram = recent.stream().mapToDouble(MeteringRecord::getVramUsedGb).average().orElse(0);
            stats.put("recentAvgUtilization", Math.round(avgUtil * 100.0) / 100.0);
            stats.put("recentAvgVramGb", Math.round(avgVram * 100.0) / 100.0);
        }
        return stats;
    }
}
