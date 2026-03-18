package com.gupex.sentinel.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {

    public static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String json = JsonUtil.toJson(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("status", statusCode);
        sendJson(exchange, statusCode, error);
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        URI uri = exchange.getRequestURI();
        String query = uri.getQuery();
        if (query != null && !query.isEmpty()) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2) {
                    params.put(pair[0], pair[1]);
                } else if (pair.length == 1) {
                    params.put(pair[0], "");
                }
            }
        }
        return params;
    }

    public static String extractPathParam(HttpExchange exchange, String basePath) {
        String path = exchange.getRequestURI().getPath();
        if (path.startsWith(basePath)) {
            String remaining = path.substring(basePath.length());
            if (remaining.startsWith("/")) {
                remaining = remaining.substring(1);
            }
            int slashIdx = remaining.indexOf('/');
            return slashIdx > 0 ? remaining.substring(0, slashIdx) : remaining;
        }
        return "";
    }

    public static void addCorsHeaders(HttpExchange exchange, String origin) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }
}
