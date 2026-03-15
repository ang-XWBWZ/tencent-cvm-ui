package com.example.tengxuncvm.web;

import java.math.BigDecimal;

public class PriceResponse {

    private BigDecimal instancePrice;
    private BigDecimal bandwidthPrice;
    private BigDecimal systemDiskPrice;
    private BigDecimal dataDiskPrice;
    private BigDecimal totalPrice;
    private String currency;
    private Boolean scheduledDestroy;
    private String zone;

    public BigDecimal getInstancePrice() {
        return instancePrice;
    }

    public void setInstancePrice(BigDecimal instancePrice) {
        this.instancePrice = instancePrice;
    }

    public BigDecimal getBandwidthPrice() {
        return bandwidthPrice;
    }

    public void setBandwidthPrice(BigDecimal bandwidthPrice) {
        this.bandwidthPrice = bandwidthPrice;
    }

    public BigDecimal getSystemDiskPrice() {
        return systemDiskPrice;
    }

    public void setSystemDiskPrice(BigDecimal systemDiskPrice) {
        this.systemDiskPrice = systemDiskPrice;
    }

    public BigDecimal getDataDiskPrice() {
        return dataDiskPrice;
    }

    public void setDataDiskPrice(BigDecimal dataDiskPrice) {
        this.dataDiskPrice = dataDiskPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getScheduledDestroy() {
        return scheduledDestroy;
    }

    public void setScheduledDestroy(Boolean scheduledDestroy) {
        this.scheduledDestroy = scheduledDestroy;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
