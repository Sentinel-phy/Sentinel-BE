package com.gupex.sentinel.model;

public class BillingPlan {
    private String id;
    private String name;
    private long monthlyFeeKrw;
    private int includedHours;
    private long hourlyRateKrw;
    private boolean priorityAllocation;
    private boolean monitoringEnabled;

    public static final BillingPlan BASIC = new BillingPlan(
        "plan_basic", "Basic", 30000, 5, 3000, false, false
    );
    public static final BillingPlan PRO = new BillingPlan(
        "plan_pro", "Pro", 100000, 20, 2500, true, true
    );

    public BillingPlan() {}

    public BillingPlan(String id, String name, long monthlyFeeKrw, int includedHours,
                       long hourlyRateKrw, boolean priorityAllocation, boolean monitoringEnabled) {
        this.id = id;
        this.name = name;
        this.monthlyFeeKrw = monthlyFeeKrw;
        this.includedHours = includedHours;
        this.hourlyRateKrw = hourlyRateKrw;
        this.priorityAllocation = priorityAllocation;
        this.monitoringEnabled = monitoringEnabled;
    }

    public long calculateMonthlyCost(double usedHours) {
        double extraHours = Math.max(0, usedHours - includedHours);
        return monthlyFeeKrw + (long)(extraHours * hourlyRateKrw);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getMonthlyFeeKrw() { return monthlyFeeKrw; }
    public void setMonthlyFeeKrw(long monthlyFeeKrw) { this.monthlyFeeKrw = monthlyFeeKrw; }

    public int getIncludedHours() { return includedHours; }
    public void setIncludedHours(int includedHours) { this.includedHours = includedHours; }

    public long getHourlyRateKrw() { return hourlyRateKrw; }
    public void setHourlyRateKrw(long hourlyRateKrw) { this.hourlyRateKrw = hourlyRateKrw; }

    public boolean isPriorityAllocation() { return priorityAllocation; }
    public void setPriorityAllocation(boolean priorityAllocation) { this.priorityAllocation = priorityAllocation; }

    public boolean isMonitoringEnabled() { return monitoringEnabled; }
    public void setMonitoringEnabled(boolean monitoringEnabled) { this.monitoringEnabled = monitoringEnabled; }
}
