package com.example.tengxuncvm.service;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.*;

@Service
public class CvmInstanceOperationService {

    private final CvmClientFactory clientFactory;

    public CvmInstanceOperationService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public boolean startInstance(String region, String instanceId) {
        try {
            CvmClient client = clientFactory.create(region);
            StartInstancesRequest request = new StartInstancesRequest();
            request.setInstanceIds(new String[]{instanceId});
            client.StartInstances(request);
            return true;
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to start instance.", ex);
        }
    }

    public boolean stopInstance(String region, String instanceId) {
        try {
            CvmClient client = clientFactory.create(region);
            StopInstancesRequest request = new StopInstancesRequest();
            request.setInstanceIds(new String[]{instanceId});
            client.StopInstances(request);
            return true;
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to stop instance.", ex);
        }
    }

    public boolean rebootInstance(String region, String instanceId) {
        try {
            CvmClient client = clientFactory.create(region);
            RebootInstancesRequest request = new RebootInstancesRequest();
            request.setInstanceIds(new String[]{instanceId});
            client.RebootInstances(request);
            return true;
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to reboot instance.", ex);
        }
    }

    public boolean modifyInstanceAttribute(String region, String instanceId, String instanceName) {
        try {
            CvmClient client = clientFactory.create(region);
            ModifyInstancesAttributeRequest request = new ModifyInstancesAttributeRequest();
            request.setInstanceIds(new String[]{instanceId});
            if (instanceName != null && !instanceName.trim().isEmpty()) {
                request.setInstanceName(instanceName);
            }
            client.ModifyInstancesAttribute(request);
            return true;
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to modify instance attribute.", ex);
        }
    }
}