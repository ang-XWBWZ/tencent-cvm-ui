package com.example.tengxuncvm.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BillDto {

    private String billId;
    private String productName;
    private String billingCycle;
    private LocalDate billDate;
    private BigDecimal totalCost;
    private BigDecimal cashPayAmount;
    private BigDecimal voucherPayAmount;
    private BigDecimal taxAmount;
    private String currency;

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getCashPayAmount() {
        return cashPayAmount;
    }

    public void setCashPayAmount(BigDecimal cashPayAmount) {
        this.cashPayAmount = cashPayAmount;
    }

    public BigDecimal getVoucherPayAmount() {
        return voucherPayAmount;
    }

    public void setVoucherPayAmount(BigDecimal voucherPayAmount) {
        this.voucherPayAmount = voucherPayAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}