package com.gupex.sentinel.handler;

import com.gupex.sentinel.service.AlertService;
import com.gupex.sentinel.service.MonitoringService;
import com.gupex.sentinel.util.HttpUtil;
import com.gupex.sentinel.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public class MonitoringHandler extends BaseHandler {

    private static final String BASE_PATH = "/api/monitoring";
    private final MonitoringService monitoringService;
    private final AlertService alertService;

    public MonitoringHandler(MonitoringService monitoringService, AlertService alertService) {
        this.monitoringService = monitoringService;
        this.alertService = alertService;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Map<String, String> params = HttpUtil.parseQueryParams(exchange);

        if (path.equals(BASE_PATH + "/drift")) {
            // GET /api/monitoring/drift
            HttpUtil.sendJson(exchange, 200, monitoringService.getDriftSummary());

        } else if (path.equals(BASE_PATH + "/comparisons")) {
            // GET /api/monitoring/comparisons?limit=20
            int limit = Integer.parseInt(params.getOrDefault("limit", "20"));
            var comparisons = monitoringService.getRecentComparisons(limit);
            HttpUtil.sendJson(exchange, 200, Map.of("comparisons", comparisons, "count", comparisons.size()));

        } else if (path.startsWith(BASE_PATH + "/session/")) {
            // GET /api/monitoring/session/{sessionId}
            String sessionId = HttpUtil.extractPathParam(exchange, BASE_PATH + "/session");
            var comparisons = monitoringService.getSessionComparisons(sessionId);
            HttpUtil.sendJson(exchange, 200, Map.of("comparisons", comparisons, "count", comparisons.size()));

        } else if (path.equals(BASE_PATH + "/alerts")) {
            // GET /api/monitoring/alerts?all=true
            boolean showAll = "true".equals(params.get("all"));
            var alerts = showAll ? alertService.getAllAlerts() : alertService.getActiveAlerts();
            HttpUtil.sendJson(exchange, 200, Map.of("alerts", alerts, "count", alerts.size()));

        } else if (path.equals(BASE_PATH + "/alerts/stats")) {
            // GET /api/monitoring/alerts/stats
            HttpUtil.sendJson(exchange, 200, alertService.getAlertStats());

        } else {
            HttpUtil.sendError(exchange, 404, "Unknown monitoring endpoint: " + path);
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String body = HttpUtil.readBody(exchange);

        if (path.equals(BASE_PATH + "/compare")) {
            // POST /api/monitoring/compare — Forge → Sentinel Sim2Real comparison
            Map<?, ?> req = JsonUtil.fromJson(body, Map.class);
            String sessionId = (String) req.get("session_id");
            String forgeProjectId = (String) req.get("forge_project_id");

            Map<?, ?> prediction = (Map<?, ?>) req.get("lstm_prediction");
            double predictedThroughput = ((Number) prediction.get("throughput")).doubleValue();
            double predictedGap = ((Number) prediction.get("gap_percent")).doubleValue();
            double confidence = ((Number) prediction.get("confidence")).doubleValue();

            Map<?, ?> actual = (Map<?, ?>) req.get("actual_performance");
            double actualThroughput = ((Number) actual.get("throughput")).doubleValue();
            double actualGap = ((Number) actual.get("measured_gap")).doubleValue();

            var comparison = monitoringService.submitComparison(
                    sessionId, forgeProjectId,
                    predictedThroughput, predictedGap, confidence,
                    actualThroughput, actualGap
            );
            HttpUtil.sendJson(exchange, 201, comparison);

        } else if (path.endsWith("/acknowledge")) {
            // POST /api/monitoring/alerts/{id}/acknowledge
            String alertId = path.replace(BASE_PATH + "/alerts/", "").replace("/acknowledge", "");
            var alert = alertService.acknowledgeAlert(alertId);
            if (alert.isPresent()) {
                HttpUtil.sendJson(exchange, 200, alert.get());
            } else {
                HttpUtil.sendError(exchange, 404, "Alert not found: " + alertId);
            }

        } else {
            HttpUtil.sendError(exchange, 404, "Unknown monitoring endpoint: " + path);
        }
    }
}
