package com.gupex.sentinel.service;

import com.gupex.sentinel.model.Alert;
import com.gupex.sentinel.store.MonitoringStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final MonitoringStore monitoringStore;
    private final AtomicInteger alertCounter = new AtomicInteger(0);

    public AlertService(MonitoringStore monitoringStore) {
        this.monitoringStore = monitoringStore;
    }

    public Alert createAlert(String sessionId, Alert.AlertLevel level, String category, String message) {
        String alertId = "alert_" + String.format("%04d", alertCounter.incrementAndGet());
        Alert alert = new Alert(alertId, sessionId, level, category, message);
        monitoringStore.saveAlert(alert);
        log.warn("ALERT [{}] {}: {} (session: {})", level, category, message, sessionId);
        return alert;
    }

    public void checkGpuUtilization(String sessionId, double utilization) {
        if (utilization > 95.0) {
            createAlert(sessionId, Alert.AlertLevel.WARNING, "GPU_UTILIZATION",
                    String.format("GPU utilization at %.1f%% - consider scaling", utilization));
        }
    }

    public void checkSim2RealDrift(String sessionId, double driftPercent) {
        if (driftPercent > 5.0) {
            createAlert(sessionId, Alert.AlertLevel.CRITICAL, "SIM2REAL_DRIFT",
                    String.format("Sim2Real drift at %.1f%% - model retraining recommended", driftPercent));
        }
    }

    public List<Alert> getActiveAlerts() {
        return monitoringStore.findActiveAlerts();
    }

    public List<Alert> getAllAlerts() {
        return monitoringStore.findAllAlerts();
    }

    public Optional<Alert> acknowledgeAlert(String alertId) {
        Optional<Alert> alert = monitoringStore.findAlertById(alertId);
        alert.ifPresent(a -> {
            a.setAcknowledged(true);
            monitoringStore.saveAlert(a);
            log.info("Alert {} acknowledged", alertId);
        });
        return alert;
    }

    public Map<String, Object> getAlertStats() {
        List<Alert> all = monitoringStore.findAllAlerts();
        long active = all.stream().filter(a -> !a.isAcknowledged()).count();
        long warnings = all.stream().filter(a -> a.getLevel() == Alert.AlertLevel.WARNING && !a.isAcknowledged()).count();
        long criticals = all.stream().filter(a -> a.getLevel() == Alert.AlertLevel.CRITICAL && !a.isAcknowledged()).count();

        return Map.of(
                "total", all.size(),
                "active", active,
                "warnings", warnings,
                "criticals", criticals
        );
    }
}
