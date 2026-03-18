package com.gupex.sentinel.store;

import com.gupex.sentinel.model.MeteringRecord;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * In-memory metering store for PoC.
 * Production: InfluxDB for time-series metrics
 */
public class MeteringStore {

    private final Deque<MeteringRecord> records = new ConcurrentLinkedDeque<>();
    private static final int MAX_RECORDS = 100_000;

    public void save(MeteringRecord record) {
        records.addLast(record);
        while (records.size() > MAX_RECORDS) {
            records.pollFirst();
        }
    }

    public List<MeteringRecord> findBySessionId(String sessionId) {
        return records.stream()
                .filter(r -> sessionId.equals(r.getSessionId()))
                .sorted(Comparator.comparing(MeteringRecord::getTimestamp))
                .collect(Collectors.toList());
    }

    public List<MeteringRecord> findByGpuInstanceId(String gpuInstanceId) {
        return records.stream()
                .filter(r -> gpuInstanceId.equals(r.getGpuInstanceId()))
                .sorted(Comparator.comparing(MeteringRecord::getTimestamp))
                .collect(Collectors.toList());
    }

    public List<MeteringRecord> findByTimeRange(Instant from, Instant to) {
        return records.stream()
                .filter(r -> !r.getTimestamp().isBefore(from) && !r.getTimestamp().isAfter(to))
                .sorted(Comparator.comparing(MeteringRecord::getTimestamp))
                .collect(Collectors.toList());
    }

    public List<MeteringRecord> getRecent(int limit) {
        List<MeteringRecord> all = new ArrayList<>(records);
        int start = Math.max(0, all.size() - limit);
        return all.subList(start, all.size());
    }

    public OptionalDouble getAverageUtilization(String sessionId) {
        return records.stream()
                .filter(r -> sessionId.equals(r.getSessionId()))
                .mapToDouble(MeteringRecord::getGpuUtilization)
                .average();
    }

    public OptionalDouble getPeakVram(String sessionId) {
        return records.stream()
                .filter(r -> sessionId.equals(r.getSessionId()))
                .mapToDouble(MeteringRecord::getVramUsedGb)
                .max();
    }

    public long getTotalRecords() {
        return records.size();
    }
}
