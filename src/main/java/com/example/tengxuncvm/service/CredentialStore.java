package com.example.tengxuncvm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tencentcloudapi.common.Credential;

@Component
public class CredentialStore {

    private static final String FALLBACK_REGION = "ap-guangzhou";

    private volatile String secretId;
    private volatile String secretKey;
    private volatile String defaultRegion;

    public CredentialStore(
            @Value("${tencentcloud.secret-id:}") String secretId,
            @Value("${tencentcloud.secret-key:}") String secretKey,
            @Value("${tencentcloud.default-region:}") String defaultRegion) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.defaultRegion = defaultRegion;
    }

    public void update(String secretId, String secretKey, String defaultRegion) {
        if (secretId != null) {
            this.secretId = secretId;
        }
        if (secretKey != null) {
            this.secretKey = secretKey;
        }
        if (defaultRegion != null) {
            this.defaultRegion = defaultRegion;
        }
    }

    public Credential getCredential() {
        if (isBlank(secretId) || isBlank(secretKey)) {
            throw new IllegalStateException("TencentCloud credentials are not configured.");
        }
        return new Credential(secretId, secretKey);
    }

    public String getDefaultRegion() {
        if (isBlank(defaultRegion)) {
            throw new IllegalStateException("tencentcloud.default-region is not configured.");
        }
        return defaultRegion;
    }

    public String getDefaultRegionOrFallback() {
        if (isBlank(defaultRegion)) {
            return FALLBACK_REGION;
        }
        return defaultRegion;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
