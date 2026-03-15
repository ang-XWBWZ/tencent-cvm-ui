package com.example.tengxuncvm.web;

public class InstanceTypeDto {

    private String zone;
    private String instanceType;
    private String instanceFamily;
    private Long cpu;
    private Long memory;
    private Long gpu;
    private Long fpga;
    private Float gpuCount;
    private String stockStatus;
    private String stockStatusCategory;
    private String soldOutReason;
    private Boolean hasStock;

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

    public String getInstanceFamily() {
        return instanceFamily;
    }

    public void setInstanceFamily(String instanceFamily) {
        this.instanceFamily = instanceFamily;
    }

    public Long getCpu() {
        return cpu;
    }

    public void setCpu(Long cpu) {
        this.cpu = cpu;
    }

    public Long getMemory() {
        return memory;
    }

    public void setMemory(Long memory) {
        this.memory = memory;
    }

    public Long getGpu() {
        return gpu;
    }

    public void setGpu(Long gpu) {
        this.gpu = gpu;
    }

    public Long getFpga() {
        return fpga;
    }

    public void setFpga(Long fpga) {
        this.fpga = fpga;
    }

    public Float getGpuCount() {
        return gpuCount;
    }

    public void setGpuCount(Float gpuCount) {
        this.gpuCount = gpuCount;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public String getStockStatusCategory() {
        return stockStatusCategory;
    }

    public void setStockStatusCategory(String stockStatusCategory) {
        this.stockStatusCategory = stockStatusCategory;
    }

    public String getSoldOutReason() {
        return soldOutReason;
    }

    public void setSoldOutReason(String soldOutReason) {
        this.soldOutReason = soldOutReason;
    }

    public Boolean getHasStock() {
        return hasStock;
    }

    public void setHasStock(Boolean hasStock) {
        this.hasStock = hasStock;
    }
}
