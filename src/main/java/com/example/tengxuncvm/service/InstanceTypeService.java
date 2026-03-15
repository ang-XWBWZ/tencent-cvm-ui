package com.example.tengxuncvm.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.DescribeZoneInstanceConfigInfosRequest;
import com.tencentcloudapi.cvm.v20170312.models.DescribeZoneInstanceConfigInfosResponse;
import com.tencentcloudapi.cvm.v20170312.models.InstanceTypeQuotaItem;

import com.example.tengxuncvm.web.InstanceTypeDto;

@Service
public class InstanceTypeService {

    private final CvmClientFactory clientFactory;

    public InstanceTypeService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public List<InstanceTypeDto> listInstanceTypes(String region) {
        try {
            CvmClient client = clientFactory.create(region);
            DescribeZoneInstanceConfigInfosRequest request = new DescribeZoneInstanceConfigInfosRequest();
            DescribeZoneInstanceConfigInfosResponse response = client.DescribeZoneInstanceConfigInfos(request);
            InstanceTypeQuotaItem[] configs = response.getInstanceTypeQuotaSet();
            if (configs == null || configs.length == 0) {
                return Collections.emptyList();
            }
            return Arrays.stream(configs)
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to list instance types.", ex);
        }
    }

    private InstanceTypeDto toDto(InstanceTypeQuotaItem config) {
        InstanceTypeDto dto = new InstanceTypeDto();
        dto.setZone(config.getZone());
        dto.setInstanceType(config.getInstanceType());
        dto.setInstanceFamily(config.getInstanceFamily());
        dto.setCpu(config.getCpu());
        dto.setMemory(config.getMemory());
        dto.setGpu(config.getGpu());
        dto.setFpga(config.getFpga());
        dto.setGpuCount(config.getGpuCount());
        dto.setStockStatus(config.getStatus());
        dto.setStockStatusCategory(config.getStatusCategory());
        dto.setSoldOutReason(config.getSoldOutReason());
        dto.setHasStock(isInStock(config.getStatus(), config.getStatusCategory(), config.getSoldOutReason()));
        return dto;
    }

    private boolean isInStock(String status, String statusCategory, String soldOutReason) {
        String s = lower(status);
        String c = lower(statusCategory);
        String r = lower(soldOutReason);
        if (s.contains("sold") && s.contains("out")) {
            return false;
        }
        if (c.contains("out") && c.contains("stock")) {
            return false;
        }
        if (r.contains("understock") || r.contains("out of stock")) {
            return false;
        }
        return true;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
