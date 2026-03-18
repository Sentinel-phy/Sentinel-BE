package com.gupex.sentinel.store;

import com.gupex.sentinel.model.GpuInstance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory GPU pool store for PoC.
 * Production: integrates with RunPod/AWS API
 */
public class GpuPoolStore {

    private final Map<String, GpuInstance> instances = new ConcurrentHashMap<>();

    public GpuPoolStore() {
        initDemoInstances();
    }

    private void initDemoInstances() {
        addInstance(new GpuInstance("gpu-001", "L40S-Node-1", "NVIDIA L40S", 48, "RunPod", 0.69));
        addInstance(new GpuInstance("gpu-002", "L40S-Node-2", "NVIDIA L40S", 48, "RunPod", 0.69));
        addInstance(new GpuInstance("gpu-003", "A100-Node-1", "NVIDIA A100", 80, "RunPod", 1.64));
    }

    public void addInstance(GpuInstance instance) {
        instances.put(instance.getId(), instance);
    }

    public Optional<GpuInstance> findById(String id) {
        return Optional.ofNullable(instances.get(id));
    }

    public List<GpuInstance> findAll() {
        return new ArrayList<>(instances.values());
    }

    public List<GpuInstance> findAvailable() {
        return instances.values().stream()
                .filter(GpuInstance::isAvailable)
                .collect(Collectors.toList());
    }

    public Optional<GpuInstance> allocate(String sessionId) {
        for (GpuInstance instance : instances.values()) {
            if (instance.isAvailable()) {
                instance.setStatus(GpuInstance.GpuInstanceStatus.ALLOCATED);
                instance.setCurrentSessionId(sessionId);
                return Optional.of(instance);
            }
        }
        return Optional.empty();
    }

    public void release(String gpuInstanceId) {
        GpuInstance instance = instances.get(gpuInstanceId);
        if (instance != null) {
            instance.setStatus(GpuInstance.GpuInstanceStatus.AVAILABLE);
            instance.setCurrentSessionId(null);
            instance.setGpuUtilization(0.0);
            instance.setVramUsedGb(0.0);
        }
    }

    public long countTotal() {
        return instances.size();
    }

    public long countAvailable() {
        return instances.values().stream().filter(GpuInstance::isAvailable).count();
    }

    public long countAllocated() {
        return instances.values().stream()
                .filter(i -> i.getStatus() == GpuInstance.GpuInstanceStatus.ALLOCATED)
                .count();
    }

    public double getAverageUtilization() {
        return instances.values().stream()
                .filter(i -> i.getStatus() == GpuInstance.GpuInstanceStatus.ALLOCATED)
                .mapToDouble(GpuInstance::getGpuUtilization)
                .average()
                .orElse(0.0);
    }
}
