package com.gupex.sentinel.service;

import com.gupex.sentinel.config.ServerConfig;
import com.gupex.sentinel.model.BillingPlan;

import java.util.*;

public class BillingService {

    private final Map<String, BillingPlan> plans = new LinkedHashMap<>();
    private final Map<String, String> userPlans = new HashMap<>(); // userId -> planId
    private final Map<String, Double> userMonthlyUsage = new HashMap<>(); // userId -> hours

    public BillingService() {
        plans.put(BillingPlan.BASIC.getId(), BillingPlan.BASIC);
        plans.put(BillingPlan.PRO.getId(), BillingPlan.PRO);
    }

    public long estimateCost(int durationHours) {
        return durationHours * ServerConfig.getHourlyRateKrw();
    }

    public long calculateCost(double hours) {
        return Math.round(hours * ServerConfig.getHourlyRateKrw());
    }

    public Map<String, Object> getUserBillingSummary(String userId) {
        String planId = userPlans.getOrDefault(userId, BillingPlan.BASIC.getId());
        BillingPlan plan = plans.get(planId);
        double usedHours = userMonthlyUsage.getOrDefault(userId, 0.0);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("userId", userId);
        summary.put("plan", plan.getName());
        summary.put("monthlyFeeKrw", plan.getMonthlyFeeKrw());
        summary.put("includedHours", plan.getIncludedHours());
        summary.put("usedHours", Math.round(usedHours * 100.0) / 100.0);
        summary.put("remainingIncludedHours", Math.max(0, plan.getIncludedHours() - usedHours));
        summary.put("extraHours", Math.max(0, usedHours - plan.getIncludedHours()));
        summary.put("totalCostKrw", plan.calculateMonthlyCost(usedHours));
        return summary;
    }

    public void recordUsage(String userId, double hours) {
        userMonthlyUsage.merge(userId, hours, Double::sum);
    }

    public List<BillingPlan> getAvailablePlans() {
        return new ArrayList<>(plans.values());
    }

    public void assignPlan(String userId, String planId) {
        if (!plans.containsKey(planId)) {
            throw new IllegalArgumentException("Unknown plan: " + planId);
        }
        userPlans.put(userId, planId);
    }
}
