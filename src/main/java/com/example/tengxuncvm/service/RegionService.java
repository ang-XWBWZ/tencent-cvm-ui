package com.example.tengxuncvm.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.DescribeRegionsRequest;
import com.tencentcloudapi.cvm.v20170312.models.DescribeRegionsResponse;
import com.tencentcloudapi.cvm.v20170312.models.RegionInfo;

@Service
public class RegionService {

    private final CvmClientFactory clientFactory;

    public RegionService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public List<String> listRegions() {
        try {
            CvmClient client = clientFactory.create(clientFactory.getDefaultRegionOrFallback());
            DescribeRegionsResponse response = client.DescribeRegions(new DescribeRegionsRequest());
            RegionInfo[] regions = response.getRegionSet();
            if (regions == null || regions.length == 0) {
                return Collections.emptyList();
            }
            return Arrays.stream(regions)
                    .map(RegionInfo::getRegion)
                    .collect(Collectors.toList());
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to list regions.", ex);
        }
    }
}
