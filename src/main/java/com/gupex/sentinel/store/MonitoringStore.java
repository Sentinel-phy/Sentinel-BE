package com.gupex.sentinel.store;

import com.gupex.sentinel.model.Alert;
import com.gupex.sentinel.model.Sim2RealComparison;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * In-memory monitoring store for PoC.
 * Production: InfluxDB + PostgreSQL
 */
public class MonitoringStore {

    private final Deque<Sim2RealComparison> comparisons = new ConcurrentLinkedDeque<>();
    private final Map<String, Alert> alerts = new ConcurrentHashMap<>();
    private static final int MAX_COMPARISONS = 10_000;

    public void saveComparison(Sim2RealComparison comparison) {
        comparisons.addLast(comparison);
        while (comparisons.size() > MAX_COMPARISONS) {
            comparisons.pollFirst();
        }
    }

    public List<Sim2RealComparison> findComparisonsBySessionId(String sessionId) {
        return comparisons.stream()
                .filter(c -> sessionId.equals(c.getSessionId()))
                .sorted(Comparator.comparing(Sim2RealComparison::getTimestamp))
                .collect(Collectors.toList());
    }

    public List<Sim2RealComparison> getRecentComparisons(int limit) {
        List<Sim2RealComparison> all = new ArrayList<>(comparisons);
        int start = Math.max(0, all.size() - limit);
        return all.subList(start, all.size());
    }

    public void saveAlert(Alert alert) {
        alerts.put(alert.getId(), alert);
    }

    public Optional<Alert> findAlertById(String id) {
        return Optional.ofNullable(alerts.get(id));
    }

    public List<Alert> findActiveAlerts() {
        return alerts.values().stream()
                .filter(a -> !a.isAcknowledged())
                .sorted(Comparator.comparing(Alert::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public List<Alert> findAllAlerts() {
        return alerts.values().stream()
                .sorted(Comparator.comparing(Alert::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public long countActiveAlerts() {
        return alerts.values().stream().filter(a -> !a.isAcknowledged()).count();
    }
}
