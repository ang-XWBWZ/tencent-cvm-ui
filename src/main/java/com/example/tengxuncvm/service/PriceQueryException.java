package com.example.tengxuncvm.service;

public class PriceQueryException extends RuntimeException {

    private final String code;
    private final String region;
    private final String zone;
    private final String instanceType;

    public PriceQueryException(String code, String message, String region, String zone, String instanceType, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.region = region;
        this.zone = zone;
        this.instanceType = instanceType;
    }

    public String getCode() {
        return code;
    }

    public String getRegion() {
        return region;
    }

    public String getZone() {
        return zone;
    }

    public String getInstanceType() {
        return instanceType;
    }
}
