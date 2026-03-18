package com.gupex.sentinel.handler;

import com.gupex.sentinel.service.*;
import com.gupex.sentinel.store.GpuPoolStore;
import com.gupex.sentinel.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregated dashboard endpoint — single API call returns all dashboard data.
 */
public class DashboardHandler extends BaseHandler {

    private final SessionService sessionService;
    private final MeteringService meteringService;
    private final MonitoringService monitoringService;
    private final AlertService alertService;
    private final GpuPoolStore gpuPoolStore;

    public DashboardHandler(SessionService sessionService, MeteringService meteringService,
                            MonitoringService monitoringService, AlertService alertService,
                            GpuPoolStore gpuPoolStore) {
        this.sessionService = sessionService;
        this.meteringService = meteringService;
        this.monitoringService = monitoringService;
        this.alertService = alertService;
        this.gpuPoolStore = gpuPoolStore;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        // GPU Infrastructure
        Map<String, Object> gpu = new LinkedHashMap<>();
        gpu.put("total", gpuPoolStore.countTotal());
        gpu.put("available", gpuPoolStore.countAvailable());
        gpu.put("allocated", gpuPoolStore.countAllocated());
        gpu.put("avgUtilization", Math.round(gpuPoolStore.getAverageUtilization() * 100.0) / 100.0);
        gpu.put("instances", gpuPoolStore.findAll());
        dashboard.put("gpu", gpu);

        // Sessions
        dashboard.put("sessions", sessionService.getSessionStats());

        // Metering
        dashboard.put("metering", meteringService.getOverallStats());
        dashboard.put("recentMetrics", meteringService.getRecentMetrics(20));

        // Sim2Real Monitoring
        dashboard.put("sim2real", monitoringService.getDriftSummary());
        dashboard.put("recentComparisons", monitoringService.getRecentComparisons(10));

        // Alerts
        dashboard.put("alerts", alertService.getAlertStats());
        dashboard.put("activeAlerts", alertService.getActiveAlerts());

        HttpUtil.sendJson(exchange, 200, dashboard);
    }
}
