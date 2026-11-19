package com.anu.engineeringthoughtrecorder.domainObjects.domainUtils;

public class API {
    private String name;
    private String method;
    private String path;
    private String purpose;

    public API(String name, String method, String path, String purpose) {
        this.name = name;
        this.method = method;
        this.path = path;
        this.purpose = purpose;
    }


    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getPurpose() {
        return purpose;
    }
}
