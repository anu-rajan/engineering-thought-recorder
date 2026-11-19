package com.anu.engineeringthoughtrecorder.domainObjects;

import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.API;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.DesignComponent;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.ExternalDependency;
import com.anu.engineeringthoughtrecorder.domainObjects.domainUtils.PerformanceCapacity;

import java.util.List;

public class SystemSpecification {
    private List<DesignComponent> components;
    private List<API> apis;
    private PerformanceCapacity performanceCapacity;
    private List<ExternalDependency> externalDependencies;

    public SystemSpecification(
            List<DesignComponent> components,
            List<API> apis,
            PerformanceCapacity performanceCapacity,
            List<ExternalDependency> externalDependencies
    ) {
        this.components = components;
        this.apis = apis;
        this.performanceCapacity = performanceCapacity;
        this.externalDependencies = externalDependencies;
    }

    public List<DesignComponent> getComponents() {
        return components;
    }

    public List<API> getApis() {
        return apis;
    }

    public PerformanceCapacity getPerformanceCapacity() {
        return performanceCapacity;
    }

    public List<ExternalDependency> getExternalDependencies() {
        return externalDependencies;
    }
}
