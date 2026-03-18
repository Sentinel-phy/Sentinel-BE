package com.gupex.sentinel.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = ServerConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("Failed to load application.properties: " + e.getMessage());
        }
    }

    public static int getPort() {
        return Integer.parseInt(props.getProperty("server.port", "8090"));
    }

    public static String getCorsOrigin() {
        return props.getProperty("cors.origin", "http://localhost:5173");
    }

    public static int getMaxGpuSlots() {
        return Integer.parseInt(props.getProperty("gpu.max.slots", "10"));
    }

    public static int getMeteringIntervalSec() {
        return Integer.parseInt(props.getProperty("metering.interval.sec", "30"));
    }

    public static int getSessionMaxHours() {
        return Integer.parseInt(props.getProperty("session.max.hours", "8"));
    }

    public static long getBasePlanMonthlyKrw() {
        return Long.parseLong(props.getProperty("billing.base.monthly.krw", "30000"));
    }

    public static long getHourlyRateKrw() {
        return Long.parseLong(props.getProperty("billing.hourly.rate.krw", "3000"));
    }

    public static int getBaseIncludedHours() {
        return Integer.parseInt(props.getProperty("billing.base.included.hours", "5"));
    }

    public static double getSim2RealDriftThreshold() {
        return Double.parseDouble(props.getProperty("monitoring.drift.threshold", "5.0"));
    }

    public static String getDbUrl() {
        return props.getProperty("db.url", "jdbc:postgresql://localhost:5432/sentinel");
    }

    public static String getDbUser() {
        return props.getProperty("db.user", "sentinel");
    }

    public static String getDbPassword() {
        return props.getProperty("db.password", "sentinel");
    }

    public static String getRedisHost() {
        return props.getProperty("redis.host", "localhost");
    }

    public static int getRedisPort() {
        return Integer.parseInt(props.getProperty("redis.port", "6379"));
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
