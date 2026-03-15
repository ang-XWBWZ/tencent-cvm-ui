package com.example.tengxuncvm.service;

import org.springframework.stereotype.Component;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.cvm.v20170312.CvmClient;

@Component
public class CvmClientFactory {

    private final CredentialStore credentialStore;

    public CvmClientFactory(CredentialStore credentialStore) {
        this.credentialStore = credentialStore;
    }

    public CvmClient create(String region) {
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("Region is required to create CvmClient.");
        }
        Credential credential = credentialStore.getCredential();
        return new CvmClient(credential, region);
    }

    public String getDefaultRegion() {
        return credentialStore.getDefaultRegion();
    }

    public String getDefaultRegionOrFallback() {
        return credentialStore.getDefaultRegionOrFallback();
    }
}
