package com.gupex.sentinel.handler;

import com.gupex.sentinel.model.Session;
import com.gupex.sentinel.service.SessionService;
import com.gupex.sentinel.util.HttpUtil;
import com.gupex.sentinel.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SessionHandler extends BaseHandler {

    private static final String BASE_PATH = "/api/session";
    private final SessionService sessionService;

    public SessionHandler(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Map<String, String> params = HttpUtil.parseQueryParams(exchange);

        if (path.equals(BASE_PATH) || path.equals(BASE_PATH + "/")) {
            // GET /api/session?userId=xxx - list sessions
            String userId = params.get("userId");
            List<Session> sessions;
            if (userId != null && !userId.isEmpty()) {
                sessions = sessionService.getUserSessions(userId);
            } else {
                sessions = sessionService.getAllSessions();
            }
            HttpUtil.sendJson(exchange, 200, Map.of("sessions", sessions, "count", sessions.size()));

        } else if (path.equals(BASE_PATH + "/active")) {
            // GET /api/session/active
            List<Session> active = sessionService.getActiveSessions();
            HttpUtil.sendJson(exchange, 200, Map.of("sessions", active, "count", active.size()));

        } else if (path.equals(BASE_PATH + "/stats")) {
            // GET /api/session/stats
            HttpUtil.sendJson(exchange, 200, sessionService.getSessionStats());

        } else {
            // GET /api/session/{id}
            String sessionId = HttpUtil.extractPathParam(exchange, BASE_PATH);
            if (sessionId.isEmpty()) {
                HttpUtil.sendError(exchange, 400, "Session ID required");
                return;
            }
            Session session = sessionService.getSession(sessionId)
                    .orElseThrow(() -> new java.util.NoSuchElementException("Session not found: " + sessionId));
            HttpUtil.sendJson(exchange, 200, session);
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String body = HttpUtil.readBody(exchange);

        if (path.equals(BASE_PATH) || path.equals(BASE_PATH + "/") || path.equals(BASE_PATH + "/create")) {
            // POST /api/session/create
            Map<?, ?> req = JsonUtil.fromJson(body, Map.class);
            String userId = (String) req.get("user_id");
            int durationHours = ((Number) req.get("duration_hours")).intValue();
            String environmentUsd = req.containsKey("environment_usd") ? (String) req.get("environment_usd") : "default.usd";
            String forgeProjectId = (String) req.get("forge_project_id");

            if (userId == null || userId.isEmpty()) {
                HttpUtil.sendError(exchange, 400, "user_id is required");
                return;
            }

            Session session = sessionService.createSession(userId, durationHours, environmentUsd, forgeProjectId);
            HttpUtil.sendJson(exchange, 201, session);

        } else if (path.endsWith("/pause")) {
            // POST /api/session/{id}/pause
            String sessionId = extractSessionIdFromAction(path, "/pause");
            Session session = sessionService.pauseSession(sessionId);
            HttpUtil.sendJson(exchange, 200, session);

        } else if (path.endsWith("/resume")) {
            // POST /api/session/{id}/resume
            String sessionId = extractSessionIdFromAction(path, "/resume");
            Session session = sessionService.resumeSession(sessionId);
            HttpUtil.sendJson(exchange, 200, session);

        } else if (path.endsWith("/terminate")) {
            // POST /api/session/{id}/terminate
            String sessionId = extractSessionIdFromAction(path, "/terminate");
            Session session = sessionService.terminateSession(sessionId);
            HttpUtil.sendJson(exchange, 200, session);

        } else {
            HttpUtil.sendError(exchange, 404, "Unknown endpoint: " + path);
        }
    }

    private String extractSessionIdFromAction(String path, String action) {
        // /api/session/{id}/action -> extract {id}
        String withoutAction = path.substring(0, path.length() - action.length());
        int lastSlash = withoutAction.lastIndexOf('/');
        return withoutAction.substring(lastSlash + 1);
    }
}
