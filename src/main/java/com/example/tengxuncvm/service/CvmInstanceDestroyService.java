package com.example.tengxuncvm.service;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.TerminateInstancesRequest;

@Service
public class CvmInstanceDestroyService {

    private final CvmClientFactory clientFactory;

    public CvmInstanceDestroyService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public boolean destroyInstance(String region, String instanceId) {
        try {
            CvmClient client = clientFactory.create(region);
            TerminateInstancesRequest request = new TerminateInstancesRequest();
            request.setInstanceIds(new String[] { instanceId });
            client.TerminateInstances(request);
            return true;
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to destroy instance.", ex);
        }
    }
}
