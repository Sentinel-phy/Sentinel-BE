package com.gupex.sentinel.handler;

import com.gupex.sentinel.service.BillingService;
import com.gupex.sentinel.service.MeteringService;
import com.gupex.sentinel.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public class MeteringHandler extends BaseHandler {

    private static final String BASE_PATH = "/api/metering";
    private final MeteringService meteringService;
    private final BillingService billingService;

    public MeteringHandler(MeteringService meteringService, BillingService billingService) {
        this.meteringService = meteringService;
        this.billingService = billingService;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Map<String, String> params = HttpUtil.parseQueryParams(exchange);

        if (path.equals(BASE_PATH + "/stats")) {
            // GET /api/metering/stats
            HttpUtil.sendJson(exchange, 200, meteringService.getOverallStats());

        } else if (path.equals(BASE_PATH + "/recent")) {
            // GET /api/metering/recent?limit=50
            int limit = Integer.parseInt(params.getOrDefault("limit", "50"));
            var records = meteringService.getRecentMetrics(limit);
            HttpUtil.sendJson(exchange, 200, Map.of("records", records, "count", records.size()));

        } else if (path.startsWith(BASE_PATH + "/session/")) {
            // GET /api/metering/session/{sessionId}
            String sessionId = HttpUtil.extractPathParam(exchange, BASE_PATH + "/session");
            if (path.contains("/summary")) {
                HttpUtil.sendJson(exchange, 200, meteringService.getSessionMeteringSummary(sessionId));
            } else {
                var records = meteringService.getSessionMetrics(sessionId);
                HttpUtil.sendJson(exchange, 200, Map.of("records", records, "count", records.size()));
            }

        } else if (path.startsWith(BASE_PATH + "/gpu/")) {
            // GET /api/metering/gpu/{gpuId}
            String gpuId = HttpUtil.extractPathParam(exchange, BASE_PATH + "/gpu");
            var records = meteringService.getGpuMetrics(gpuId);
            HttpUtil.sendJson(exchange, 200, Map.of("records", records, "count", records.size()));

        } else if (path.equals(BASE_PATH + "/billing")) {
            // GET /api/metering/billing?userId=xxx
            String userId = params.getOrDefault("userId", "user_default");
            HttpUtil.sendJson(exchange, 200, billingService.getUserBillingSummary(userId));

        } else if (path.equals(BASE_PATH + "/plans")) {
            // GET /api/metering/plans
            HttpUtil.sendJson(exchange, 200, Map.of("plans", billingService.getAvailablePlans()));

        } else {
            HttpUtil.sendError(exchange, 404, "Unknown metering endpoint: " + path);
        }
    }
}
