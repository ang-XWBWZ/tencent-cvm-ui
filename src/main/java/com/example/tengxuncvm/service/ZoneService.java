package com.example.tengxuncvm.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.DescribeZonesRequest;
import com.tencentcloudapi.cvm.v20170312.models.DescribeZonesResponse;
import com.tencentcloudapi.cvm.v20170312.models.ZoneInfo;

@Service
public class ZoneService {

    private final CvmClientFactory clientFactory;

    public ZoneService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public List<String> listZones(String region) {
        try {
            CvmClient client = clientFactory.create(region);
            DescribeZonesRequest request = new DescribeZonesRequest();
            DescribeZonesResponse response = client.DescribeZones(request);
            ZoneInfo[] zones = response.getZoneSet();
            if (zones == null || zones.length == 0) {
                return Collections.emptyList();
            }
            return Arrays.stream(zones)
                    .map(ZoneInfo::getZone)
                    .collect(Collectors.toList());
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to list zones.", ex);
        }
    }
}
