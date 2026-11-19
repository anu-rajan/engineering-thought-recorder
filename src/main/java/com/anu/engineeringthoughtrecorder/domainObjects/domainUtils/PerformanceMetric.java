package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

public class PerformanceMetric {
    private String metric;
    private String value;
    private String unit;

    public PerformanceMetric(String metric, String value, String unit) {
        this.metric = metric;
        this.value = value;
        this.unit = unit;
    }

    public String getMetric() {
        return metric;
    }

    public String getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }
}
