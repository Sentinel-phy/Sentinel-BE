package com.gupex.sentinel.handler;

import com.gupex.sentinel.store.GpuPoolStore;
import com.gupex.sentinel.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

public class GpuPoolHandler extends BaseHandler {

    private final GpuPoolStore gpuPoolStore;

    public GpuPoolHandler(GpuPoolStore gpuPoolStore) {
        this.gpuPoolStore = gpuPoolStore;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/available")) {
            var available = gpuPoolStore.findAvailable();
            HttpUtil.sendJson(exchange, 200, Map.of("instances", available, "count", available.size()));
        } else {
            var all = gpuPoolStore.findAll();
            HttpUtil.sendJson(exchange, 200, Map.of(
                    "instances", all,
                    "total", gpuPoolStore.countTotal(),
                    "available", gpuPoolStore.countAvailable(),
                    "allocated", gpuPoolStore.countAllocated(),
                    "avgUtilization", Math.round(gpuPoolStore.getAverageUtilization() * 100.0) / 100.0
            ));
        }
    }
}
