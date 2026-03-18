package com.gupex.sentinel;

import com.gupex.sentinel.config.ServerConfig;
import com.gupex.sentinel.handler.*;
import com.gupex.sentinel.service.*;
import com.gupex.sentinel.store.*;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Sentinel v1.0 — Physical AI GPU Sharing Platform
 *
 * Pure Java 21 server with Virtual Threads.
 * No framework dependencies.
 */
public class SentinelServer {

    private static final Logger log = LoggerFactory.getLogger(SentinelServer.class);

    public static void main(String[] args) throws IOException {
        int port = ServerConfig.getPort();

        // Initialize stores (in-memory for PoC)
        SessionStore sessionStore = new SessionStore();
        GpuPoolStore gpuPoolStore = new GpuPoolStore();
        MeteringStore meteringStore = new MeteringStore();
        MonitoringStore monitoringStore = new MonitoringStore();

        // Initialize services
        AlertService alertService = new AlertService(monitoringStore);
        BillingService billingService = new BillingService();
        SessionService sessionService = new SessionService(sessionStore, gpuPoolStore, billingService);
        MeteringService meteringService = new MeteringService(meteringStore, sessionStore, gpuPoolStore, alertService);
        MonitoringService monitoringService = new MonitoringService(monitoringStore, alertService);

        // Generate demo data for Sim2Real monitoring
        monitoringService.generateDemoData();

        // Start metering collection (30-second intervals)
        meteringService.startCollection(ServerConfig.getMeteringIntervalSec());

        // Create HTTP server with Virtual Threads
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        // Health
        server.createContext("/api/health", new HealthHandler());

        // Dashboard (aggregated)
        server.createContext("/api/dashboard", new DashboardHandler(
                sessionService, meteringService, monitoringService, alertService, gpuPoolStore));

        // Session management
        server.createContext("/api/session", new SessionHandler(sessionService));

        // GPU pool
        server.createContext("/api/gpu", new GpuPoolHandler(gpuPoolStore));

        // Metering + Billing
        server.createContext("/api/metering", new MeteringHandler(meteringService, billingService));

        // Monitoring (Sim2Real + Alerts)
        server.createContext("/api/monitoring", new MonitoringHandler(monitoringService, alertService));

        server.start();

        log.info("===========================================");
        log.info("  Sentinel v1.0 — Physical AI GPU Platform");
        log.info("  Port: {}", port);
        log.info("  CORS: {}", ServerConfig.getCorsOrigin());
        log.info("  GPU Pool: {} instances", gpuPoolStore.countTotal());
        log.info("  Virtual Threads: enabled (Java 21)");
        log.info("===========================================");
        log.info("API Endpoints:");
        log.info("  GET  /api/health");
        log.info("  GET  /api/dashboard");
        log.info("  GET  /api/session         — list sessions");
        log.info("  GET  /api/session/active   — active sessions");
        log.info("  GET  /api/session/stats    — session statistics");
        log.info("  POST /api/session/create   — create session");
        log.info("  POST /api/session/{id}/pause");
        log.info("  POST /api/session/{id}/resume");
        log.info("  POST /api/session/{id}/terminate");
        log.info("  GET  /api/gpu              — GPU pool status");
        log.info("  GET  /api/gpu/available     — available GPUs");
        log.info("  GET  /api/metering/stats    — metering stats");
        log.info("  GET  /api/metering/recent   — recent metrics");
        log.info("  GET  /api/metering/billing  — billing summary");
        log.info("  GET  /api/metering/plans    — billing plans");
        log.info("  GET  /api/monitoring/drift   — Sim2Real drift");
        log.info("  POST /api/monitoring/compare — submit comparison");
        log.info("  GET  /api/monitoring/alerts   — active alerts");
        log.info("===========================================");

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Sentinel...");
            meteringService.stopCollection();
            server.stop(3);
            log.info("Sentinel stopped.");
        }));
    }
}
