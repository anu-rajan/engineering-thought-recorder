package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

public class ExternalDependency {

    private String name;
    private String purpose;
    private String requirement;

    public ExternalDependency(String name, String purpose, String requirement) {
        this.name = name;
        this.purpose = purpose;
        this.requirement = requirement;
    }

    public String getName() {
        return name;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getRequirement() {
        return requirement;
    }
}
