package com.example.tengxuncvm.service;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.DescribeInstancesRequest;
import com.tencentcloudapi.cvm.v20170312.models.DescribeInstancesResponse;

@Service
public class CvmInstanceCheckService {

    private final CvmClientFactory clientFactory;

    public CvmInstanceCheckService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public boolean checkInstance(String instanceId) {
        try {
            CvmClient client = clientFactory.create(clientFactory.getDefaultRegion());
            DescribeInstancesRequest request = new DescribeInstancesRequest();
            request.setInstanceIds(new String[] { instanceId });
            DescribeInstancesResponse response = client.DescribeInstances(request);
            return response.getInstanceSet() != null && response.getInstanceSet().length > 0;
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to check instance.", ex);
        }
    }
}
