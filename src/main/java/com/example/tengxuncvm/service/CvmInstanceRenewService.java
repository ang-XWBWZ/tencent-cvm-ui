package com.example.tengxuncvm.service;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.InstanceChargePrepaid;
import com.tencentcloudapi.cvm.v20170312.models.RenewInstancesRequest;

@Service
public class CvmInstanceRenewService {

    private final CvmClientFactory clientFactory;

    public CvmInstanceRenewService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public boolean renewInstance(String region, String instanceId, int periodMonths) {
        try {
            CvmClient client = clientFactory.create(region);
            RenewInstancesRequest request = new RenewInstancesRequest();
            request.setInstanceIds(new String[] { instanceId });

            InstanceChargePrepaid prepaid = new InstanceChargePrepaid();
            prepaid.setPeriod((long) periodMonths);
            prepaid.setRenewFlag("NOTIFY_AND_MANUAL_RENEW");
            request.setInstanceChargePrepaid(prepaid);

            client.RenewInstances(request);
            return true;
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to renew instance.", ex);
        }
    }
}
