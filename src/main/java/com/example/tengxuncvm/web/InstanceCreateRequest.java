package com.example.tengxuncvm.web;

import java.util.List;

public class InstanceCreateRequest {

    private String region;
    private String zone;
    private String instanceType;
    private String imageId;
    private String instanceName;
    private Integer systemDiskGb;
    private Integer dataDiskGb;
    private Integer bandwidthMbps;
    private String bandwidthChargeType;
    private String instanceChargeType;
    private String password;
    private List<String> keyIds;
    private List<String> securityGroupIds;
    private String userData;
    private Boolean scheduledDestroy;
    private String scheduledDestroyTime;
    private Integer instanceCount;

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

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getKeyIds() {
        return keyIds;
    }

    public void setKeyIds(List<String> keyIds) {
        this.keyIds = keyIds;
    }

    public List<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
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

    public String getScheduledDestroyTime() {
        return scheduledDestroyTime;
    }

    public void setScheduledDestroyTime(String scheduledDestroyTime) {
        this.scheduledDestroyTime = scheduledDestroyTime;
    }

    public Integer getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }
}