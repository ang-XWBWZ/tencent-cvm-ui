package com.example.tengxuncvm.web;

public class PriceRequest {

    private String region;
    private String zone;
    private String instanceType;
    private String imageId;
    private Integer systemDiskGb;
    private Integer dataDiskGb;
    private Integer bandwidthMbps;
    private String bandwidthChargeType;
    private String instanceChargeType;
    private String userData;
    private Boolean scheduledDestroy;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public Integer getSystemDiskGb() {
        return systemDiskGb;
    }

    public void setSystemDiskGb(Integer systemDiskGb) {
        this.systemDiskGb = systemDiskGb;
    }

    public Integer getDataDiskGb() {
        return dataDiskGb;
    }

    public void setDataDiskGb(Integer dataDiskGb) {
        this.dataDiskGb = dataDiskGb;
    }

    public Integer getBandwidthMbps() {
        return bandwidthMbps;
    }

    public void setBandwidthMbps(Integer bandwidthMbps) {
        this.bandwidthMbps = bandwidthMbps;
    }

    public String getBandwidthChargeType() {
        return bandwidthChargeType;
    }

    public void setBandwidthChargeType(String bandwidthChargeType) {
        this.bandwidthChargeType = bandwidthChargeType;
    }

    public String getInstanceChargeType() {
        return instanceChargeType;
    }

    public void setInstanceChargeType(String instanceChargeType) {
        this.instanceChargeType = instanceChargeType;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public Boolean getScheduledDestroy() {
        return scheduledDestroy;
    }

    public void setScheduledDestroy(Boolean scheduledDestroy) {
        this.scheduledDestroy = scheduledDestroy;
    }
}
