package com.example.tengxuncvm.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.DescribeInstancesRequest;
import com.tencentcloudapi.cvm.v20170312.models.DescribeInstancesResponse;
import com.tencentcloudapi.cvm.v20170312.models.Instance;

import com.example.tengxuncvm.web.InstanceDto;
import com.example.tengxuncvm.web.InstanceListResponse;

@Service
public class InstanceService {

    private final CvmClientFactory clientFactory;

    public InstanceService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public InstanceListResponse listInstances(String region, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(size, 50));
        long offset = (long) (safePage - 1) * safeSize;

        try {
            CvmClient client = clientFactory.create(region);
            DescribeInstancesRequest request = new DescribeInstancesRequest();
            request.setOffset(offset);
            request.setLimit((long) safeSize);
            DescribeInstancesResponse response = client.DescribeInstances(request);

            Instance[] instances = response.getInstanceSet();
            List<InstanceDto> items;
            if (instances == null || instances.length == 0) {
                items = Collections.emptyList();
            } else {
                items = Arrays.stream(instances)
                        .map(this::toDto)
                        .collect(Collectors.toList());
            }

            InstanceListResponse result = new InstanceListResponse();
            result.setTotalCount(response.getTotalCount());
            result.setPage(safePage);
            result.setSize(safeSize);
            result.setItems(items);
            return result;
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to list instances.", ex);
        }
    }

    private InstanceDto toDto(Instance instance) {
        InstanceDto dto = new InstanceDto();
        dto.setInstanceId(instance.getInstanceId());
        dto.setInstanceName(instance.getInstanceName());
        dto.setInstanceType(instance.getInstanceType());
        dto.setCpu(instance.getCPU());
        dto.setMemory(instance.getMemory());
        if (instance.getPlacement() != null) {
            dto.setZone(instance.getPlacement().getZone());
        }
        dto.setInstanceChargeType(instance.getInstanceChargeType());
        dto.setInstanceState(instance.getInstanceState());

        if (instance.getPrivateIpAddresses() != null && instance.getPrivateIpAddresses().length > 0) {
            dto.setPrivateIp(instance.getPrivateIpAddresses()[0]);
        }
        if (instance.getPublicIpAddresses() != null && instance.getPublicIpAddresses().length > 0) {
            dto.setPublicIp(instance.getPublicIpAddresses()[0]);
        }
        return dto;
    }
}
