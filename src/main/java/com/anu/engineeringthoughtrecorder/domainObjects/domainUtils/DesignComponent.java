package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

public class DesignComponent {
    private String name;
    private String responsibility;
    private String technology;

    public DesignComponent(String name, String responsibility, String technology) {
        this.name = name;
        this.responsibility = responsibility;
        this.technology = technology;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getResponsibility() {
        return responsibility;
    }
    public void setResponsibility(String responsibility) {
        this.responsibility = responsibility;
    }
    public String getTechnology() {
        return technology;
    }
}
