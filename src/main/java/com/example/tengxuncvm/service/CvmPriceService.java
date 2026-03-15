package com.example.tengxuncvm.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.DataDisk;
import com.tencentcloudapi.cvm.v20170312.models.InquiryPriceRunInstancesRequest;
import com.tencentcloudapi.cvm.v20170312.models.InquiryPriceRunInstancesResponse;
import com.tencentcloudapi.cvm.v20170312.models.InternetAccessible;
import com.tencentcloudapi.cvm.v20170312.models.ItemPrice;
import com.tencentcloudapi.cvm.v20170312.models.Placement;
import com.tencentcloudapi.cvm.v20170312.models.Price;
import com.tencentcloudapi.cvm.v20170312.models.SystemDisk;

import com.example.tengxuncvm.web.PriceRequest;
import com.example.tengxuncvm.web.PriceResponse;

@Service
public class CvmPriceService {

    private final CvmClientFactory clientFactory;
    private final ZoneService zoneService;

    public CvmPriceService(CvmClientFactory clientFactory, ZoneService zoneService) {
        this.clientFactory = clientFactory;
        this.zoneService = zoneService;
    }

    public PriceResponse quotePrice(PriceRequest priceRequest) {
        String region = normalizeInput(priceRequest.getRegion());
        String instanceType = normalizeInput(priceRequest.getInstanceType());
        if (region == null) {
            throw new PriceQueryException("MISSING_REGION", "Region is required.", null, null, instanceType, null);
        }
        if (instanceType == null) {
            throw new PriceQueryException("MISSING_INSTANCE_TYPE", "Instance type is required.", region, null, null, null);
        }
        List<String> zones = zoneService.listZones(region);
        if (zones.isEmpty()) {
            throw new PriceQueryException("NO_ZONES", "No zones found for region: " + region, region, null, instanceType, null);
        }

        CvmClient client = clientFactory.create(region);
        String requestedZone = normalizeInput(priceRequest.getZone());
        List<String> candidates = buildZoneCandidates(zones, requestedZone);
        TencentCloudSDKException lastUnsupported = null;

        for (String zone : candidates) {
            InquiryPriceRunInstancesRequest sdkRequest = buildRequest(priceRequest, instanceType, zone);
            try {
                InquiryPriceRunInstancesResponse response = client.InquiryPriceRunInstances(sdkRequest);
                PriceResponse priceResponse = toPriceResponse(response, priceRequest.getScheduledDestroy());
                priceResponse.setZone(zone);
                return priceResponse;
            } catch (TencentCloudSDKException ex) {
                if (isZoneUnsupported(ex)) {
                    lastUnsupported = ex;
                    continue;
                }
                throw new PriceQueryException("PRICE_QUERY_FAILED", "Failed to query price.", region, zone, instanceType, ex);
            }
        }
        throw new PriceQueryException(
            "INSTANCE_TYPE_UNAVAILABLE",
            String.format("Instance type %s is not available in region %s.", instanceType, region),
            region,
            requestedZone,
            instanceType,
            lastUnsupported
        );
    }

    private static List<String> buildZoneCandidates(List<String> zones, String requestedZone) {
        List<String> candidates = new ArrayList<>();
        if (requestedZone != null) {
            candidates.add(requestedZone);
        }
        for (String zone : zones) {
            if (requestedZone == null || !requestedZone.equals(zone)) {
                candidates.add(zone);
            }
        }
        return candidates;
    }

    private static String normalizeInput(String input) {
        if (input == null) {
            return null;
        }
        String trimmed = input.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isZoneUnsupported(TencentCloudSDKException ex) {
        String code = ex.getErrorCode();
        if (code != null) {
            String normalized = code.toLowerCase();
            if (normalized.contains("zonenotsupport") || normalized.contains("instancetypenotsupport")) {
                return true;
            }
        }
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        String normalizedMessage = message.toLowerCase();
        return normalizedMessage.contains("not available in the current zone")
            || normalizedMessage.contains("not support in the current zone")
            || normalizedMessage.contains("zone not support");
    }

    private static InquiryPriceRunInstancesRequest buildRequest(PriceRequest priceRequest, String instanceType, String zone) {
        InquiryPriceRunInstancesRequest sdkRequest = new InquiryPriceRunInstancesRequest();
        sdkRequest.setInstanceChargeType(normalizeInstanceChargeType(priceRequest.getInstanceChargeType()));
        sdkRequest.setInstanceType(instanceType);
        sdkRequest.setInstanceCount(1L);

        Placement placement = new Placement();
        placement.setZone(zone);
        sdkRequest.setPlacement(placement);

        if (priceRequest.getImageId() != null && !priceRequest.getImageId().trim().isEmpty()) {
            sdkRequest.setImageId(priceRequest.getImageId());
        }
        if (priceRequest.getSystemDiskGb() != null) {
            SystemDisk systemDisk = new SystemDisk();
            systemDisk.setDiskSize(normalizeDiskSize(priceRequest.getSystemDiskGb()));
            sdkRequest.setSystemDisk(systemDisk);
        }
        if (priceRequest.getDataDiskGb() != null && priceRequest.getDataDiskGb() > 0) {
            DataDisk dataDisk = new DataDisk();
            dataDisk.setDiskSize(priceRequest.getDataDiskGb().longValue());
            sdkRequest.setDataDisks(new DataDisk[] { dataDisk });
        }
        if (priceRequest.getBandwidthMbps() != null && priceRequest.getBandwidthMbps() > 0) {
            InternetAccessible internetAccessible = new InternetAccessible();
            internetAccessible.setInternetMaxBandwidthOut(priceRequest.getBandwidthMbps().longValue());
            internetAccessible.setInternetChargeType(normalizeBandwidthChargeType(priceRequest.getBandwidthChargeType()));
            internetAccessible.setPublicIpAssigned(true);
            sdkRequest.setInternetAccessible(internetAccessible);
        }
        return sdkRequest;
    }

    private static String normalizeInstanceChargeType(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "POSTPAID_BY_HOUR";
        }
        return input.trim();
    }

    private static String normalizeBandwidthChargeType(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "BANDWIDTH_POSTPAID_BY_HOUR";
        }
        return input.trim();
    }

    private static Long normalizeDiskSize(Integer input) {
        if (input == null || input <= 0) {
            return 20L;
        }
        if (input < 20) {
            return 20L;
        }
        return input.longValue();
    }

    private static PriceResponse toPriceResponse(InquiryPriceRunInstancesResponse response, Boolean scheduledDestroy) {
        PriceResponse result = new PriceResponse();
        Price price = response.getPrice();
        if (price == null) {
            result.setInstancePrice(BigDecimal.ZERO);
            result.setTotalPrice(BigDecimal.ZERO);
            result.setScheduledDestroy(scheduledDestroy);
            return result;
        }

        BigDecimal instance = toBigDecimal(price.getInstancePrice());
        BigDecimal bandwidth = toBigDecimal(price.getBandwidthPrice());
        BigDecimal systemDisk = BigDecimal.ZERO;
        BigDecimal dataDisk = BigDecimal.ZERO;
        BigDecimal total = instance.add(bandwidth);

        result.setInstancePrice(instance);
        result.setBandwidthPrice(bandwidth);
        result.setSystemDiskPrice(systemDisk);
        result.setDataDiskPrice(dataDisk);
        result.setTotalPrice(total);
        result.setCurrency(null);
        result.setScheduledDestroy(scheduledDestroy);
        return result;
    }

    private static BigDecimal toBigDecimal(ItemPrice price) {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        if (price.getUnitPrice() != null) {
            return BigDecimal.valueOf(price.getUnitPrice());
        }
        if (price.getDiscountPrice() != null) {
            return BigDecimal.valueOf(price.getDiscountPrice());
        }
        return BigDecimal.ZERO;
    }
}
