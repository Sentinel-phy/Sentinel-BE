package com.gupex.sentinel.handler;

import com.gupex.sentinel.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class HealthHandler extends BaseHandler {

    private final Instant startTime = Instant.now();

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("service", "Sentinel");
        health.put("version", "1.0.0");
        health.put("uptime", Instant.now().getEpochSecond() - startTime.getEpochSecond());
        health.put("timestamp", Instant.now().toString());
        HttpUtil.sendJson(exchange, 200, health);
    }
}
