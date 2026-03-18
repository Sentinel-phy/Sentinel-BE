package com.gupex.sentinel.handler;

import com.gupex.sentinel.config.ServerConfig;
import com.gupex.sentinel.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class BaseHandler implements HttpHandler {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtil.addCorsHeaders(exchange, ServerConfig.getCorsOrigin());

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            String method = exchange.getRequestMethod().toUpperCase();
            switch (method) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "PUT" -> handlePut(exchange);
                case "DELETE" -> handleDelete(exchange);
                default -> HttpUtil.sendError(exchange, 405, "Method not allowed: " + method);
            }
        } catch (IllegalArgumentException e) {
            HttpUtil.sendError(exchange, 400, e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            HttpUtil.sendError(exchange, 404, e.getMessage());
        } catch (IllegalStateException e) {
            HttpUtil.sendError(exchange, 409, e.getMessage());
        } catch (Exception e) {
            log.error("Unhandled error in {}", exchange.getRequestURI(), e);
            HttpUtil.sendError(exchange, 500, "Internal server error");
        }
    }

    protected void handleGet(HttpExchange exchange) throws IOException {
        HttpUtil.sendError(exchange, 405, "GET not supported");
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        HttpUtil.sendError(exchange, 405, "POST not supported");
    }

    protected void handlePut(HttpExchange exchange) throws IOException {
        HttpUtil.sendError(exchange, 405, "PUT not supported");
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        HttpUtil.sendError(exchange, 405, "DELETE not supported");
    }
}
