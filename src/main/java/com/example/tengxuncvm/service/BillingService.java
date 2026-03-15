package com.example.tengxuncvm.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tengxuncvm.web.BalanceResponse;
import com.example.tengxuncvm.web.BillDto;
import com.example.tengxuncvm.web.BillListResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tencentcloudapi.common.CommonClient;
import com.tencentcloudapi.common.CommonRequest;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

@Service
public class BillingService {

    private static final String BILLING_PRODUCT = "billing";
    private static final String BILLING_VERSION = "2018-07-09";
    private static final String BILLING_REGION = "ap-guangzhou";

    private final CredentialStore credentialStore;
    private final Gson gson = new Gson();

    public BillingService(CredentialStore credentialStore) {
        this.credentialStore = credentialStore;
    }

    public BalanceResponse getAccountBalance() {
        try {
            JsonObject response = callBillingApi("DescribeAccountBalance", new JsonObject());

            BalanceResponse balance = new BalanceResponse();
            BigDecimal available = getDecimal(response, "AvailableBalance");
            BigDecimal frozen = getDecimal(response, "FreezeAmount", "FrozenBalance");
            BigDecimal total = getDecimal(response, "Balance", "TotalBalance");

            if (available == null && total != null && frozen != null) {
                available = total.subtract(frozen);
            }
            if (available == null) {
                available = BigDecimal.ZERO;
            }
            if (frozen == null) {
                frozen = BigDecimal.ZERO;
            }
            if (total == null) {
                total = available.add(frozen);
            }

            // 腾讯账单部分接口金额单位为“分”，前端按“元”展示，这里统一转换
            available = toYuan(available);
            frozen = toYuan(frozen);
            total = toYuan(total);

            balance.setAvailableBalance(available);
            balance.setFrozenBalance(frozen);
            balance.setTotalBalance(total);
            balance.setCurrency("CNY");
            return balance;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load account balance from Tencent Cloud Billing API.", ex);
        }
    }

    public BillListResponse getBills(int page, int size, String billingCycle) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, 100));

        try {
            JsonObject params = new JsonObject();

            String month = (billingCycle != null && !billingCycle.trim().isEmpty())
                    ? billingCycle.trim()
                    : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDate firstDay = LocalDate.parse(month + "-01");
            String beginTime = firstDay.toString();
            String endTime = firstDay.withDayOfMonth(firstDay.lengthOfMonth()).toString();
            // 账单接口要求时间范围；部分接口不支持 Offset/Limit/Month
            params.addProperty("BeginTime", beginTime);
            params.addProperty("EndTime", endTime);

            JsonObject response;
            try {
                response = callBillingApi("DescribeBillSummaryByProduct", params);
            } catch (Exception first) {
                response = callBillingApi("DescribeBillSummaryByPayMode", params);
            }

            JsonArray list = firstArray(response,
                    "SummaryOverview",
                    "SummaryOverviewSet",
                    "BillSummaryOverview",
                    "BillSummaryOverviewSet",
                    "List");

            List<BillDto> allItems = new ArrayList<>();
            if (list != null) {
                for (JsonElement element : list) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    allItems.add(toBillDto(element.getAsJsonObject(), billingCycle));
                }
            }

            int fromIndex = Math.min((safePage - 1) * safeSize, allItems.size());
            int toIndex = Math.min(fromIndex + safeSize, allItems.size());
            List<BillDto> pageItems = allItems.subList(fromIndex, toIndex);

            BillListResponse result = new BillListResponse();
            result.setPage(safePage);
            result.setSize(safeSize);
            result.setItems(pageItems);

            Long total = getLong(response, "Total", "TotalCount");
            result.setTotalCount(total != null ? total : (long) allItems.size());
            return result;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load bill list from Tencent Cloud Billing API.", ex);
        }
    }

    public BillDto getBillDetail(String billId) {
        BillListResponse response = getBills(1, 100, null);
        return response.getItems().stream()
                .filter(item -> billId.equals(item.getBillId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + billId));
    }

    private JsonObject callBillingApi(String action, JsonObject params) throws TencentCloudSDKException {
        Credential credential = credentialStore.getCredential();
        CommonClient client = new CommonClient(BILLING_PRODUCT, BILLING_VERSION, credential, BILLING_REGION);
        CommonRequest request = new CommonRequest(params == null ? "{}" : gson.toJson(params));

        String raw = client.commonRequest(request, action);
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalStateException("Empty response from Tencent Cloud Billing API.");
        }

        JsonObject root = gson.fromJson(raw, JsonObject.class);
        if (root == null || !root.has("Response") || !root.get("Response").isJsonObject()) {
            throw new IllegalStateException("Unexpected response format from Tencent Cloud Billing API.");
        }
        return root.getAsJsonObject("Response");
    }

    private BillDto toBillDto(JsonObject item, String billingCycleFallback) {
        BillDto dto = new BillDto();
        dto.setBillId(getString(item, "BillId", "BusinessCode", "PayModeName", "ProjectName", "ProductCode"));
        dto.setProductName(getString(item, "ProductName", "BusinessCodeName", "BusinessCode", "ComponentCodeName"));
        dto.setBillingCycle(getString(item, "BillMonth", "BillingCycle", "Month", "PayTime", "ActionTime", "Date"));
        dto.setBillDate(parseDate(getString(item, "BillDate", "Date", "ActionTime", "PayTime")));
        dto.setTotalCost(absOrNull(getDecimal(item, "RealTotalCost", "TotalCost", "Cost", "ReduceTypeTotal")));
        dto.setCashPayAmount(absOrNull(getDecimal(item, "CashPayAmount", "RealCashPayAmount", "CashPay")));
        dto.setVoucherPayAmount(absOrNull(getDecimal(item, "VoucherPayAmount", "IncentivePayAmount", "VoucherPay")));
        dto.setTaxAmount(absOrNull(getDecimal(item, "Tax", "TaxAmount", "TaxCost")));
        dto.setCurrency("CNY");

        if (dto.getBillId() == null || dto.getBillId().trim().isEmpty()) {
            dto.setBillId("bill-" + System.nanoTime());
        }
        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            dto.setProductName("UNKNOWN");
        }
        if (dto.getBillingCycle() == null || dto.getBillingCycle().trim().isEmpty()) {
            dto.setBillingCycle(billingCycleFallback == null || billingCycleFallback.trim().isEmpty()
                    ? LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                    : billingCycleFallback);
        }
        if (dto.getBillDate() == null) {
            dto.setBillDate(LocalDate.now());
        }
        if (dto.getTotalCost() == null) {
            dto.setTotalCost(BigDecimal.ZERO);
        }
        if (dto.getCashPayAmount() == null) {
            dto.setCashPayAmount(BigDecimal.ZERO);
        }
        if (dto.getVoucherPayAmount() == null) {
            dto.setVoucherPayAmount(BigDecimal.ZERO);
        }
        if (dto.getTaxAmount() == null) {
            dto.setTaxAmount(BigDecimal.ZERO);
        }

        // 统一按“元”返回
        dto.setTotalCost(toYuan(dto.getTotalCost()));
        dto.setCashPayAmount(toYuan(dto.getCashPayAmount()));
        dto.setVoucherPayAmount(toYuan(dto.getVoucherPayAmount()));
        dto.setTaxAmount(toYuan(dto.getTaxAmount()));

        return dto;
    }

    private JsonArray firstArray(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && obj.get(key).isJsonArray()) {
                return obj.getAsJsonArray(key);
            }
        }
        return null;
    }

    private String getString(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsString();
            }
        }
        return null;
    }

    private Long getLong(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                try {
                    return obj.get(key).getAsLong();
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }

    private BigDecimal getDecimal(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                try {
                    return new BigDecimal(obj.get(key).getAsString());
                } catch (Exception ignore) {
                }
            }
        }
        return null;
    }

    private BigDecimal absOrNull(BigDecimal value) {
        return value == null ? null : value.abs();
    }

    private BigDecimal toYuan(BigDecimal fen) {
        if (fen == null) {
            return BigDecimal.ZERO;
        }
        return fen.divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() >= 10) {
            normalized = normalized.substring(0, 10);
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
