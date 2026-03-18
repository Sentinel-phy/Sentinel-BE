package com.gupex.sentinel.store;

import com.gupex.sentinel.model.Session;
import com.gupex.sentinel.model.SessionStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory session store for PoC.
 * Production: PostgreSQL + Redis cache
 */
public class SessionStore {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public void save(Session session) {
        sessions.put(session.getId(), session);
    }

    public Optional<Session> findById(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    public List<Session> findByUserId(String userId) {
        return sessions.values().stream()
                .filter(s -> userId.equals(s.getUserId()))
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Session> findByStatus(SessionStatus status) {
        return sessions.values().stream()
                .filter(s -> s.getStatus() == status)
                .sorted(Comparator.comparing(Session::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<Session> findActive() {
        return sessions.values().stream()
                .filter(Session::isActive)
                .sorted(Comparator.comparing(Session::getCreatedAt))
                .collect(Collectors.toList());
    }

    public List<Session> findAll() {
        return sessions.values().stream()
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        sessions.remove(id);
    }

    public long countActive() {
        return sessions.values().stream().filter(Session::isActive).count();
    }

    public long countByStatus(SessionStatus status) {
        return sessions.values().stream().filter(s -> s.getStatus() == status).count();
    }
}
