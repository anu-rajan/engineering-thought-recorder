package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

import java.util.List;

public class PerformanceCapacity {
    private List<PerformanceMetric> metrics;

    public PerformanceCapacity(List<PerformanceMetric> metrics) {
        this.metrics = metrics;
    }

    public List<PerformanceMetric> getMetrics() {
        return metrics;
    }
}
