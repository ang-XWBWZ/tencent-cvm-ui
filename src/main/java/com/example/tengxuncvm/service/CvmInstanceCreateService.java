package com.example.tengxuncvm.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.*;
import com.example.tengxuncvm.web.InstanceCreateRequest;

@Service
public class CvmInstanceCreateService {

    private final CvmClientFactory clientFactory;

    public CvmInstanceCreateService(CvmClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public String createInstance(InstanceCreateRequest request) {
        try {
            CvmClient client = clientFactory.create(request.getRegion());
            RunInstancesRequest runRequest = new RunInstancesRequest();

            // 实例配置
            runRequest.setInstanceType(request.getInstanceType());
            runRequest.setImageId(request.getImageId());
            runRequest.setInstanceName(request.getInstanceName());
            runRequest.setInstanceCount(normalizeInstanceCount(request.getInstanceCount()));

            // 系统盘
            SystemDisk systemDisk = new SystemDisk();
            if (request.getSystemDiskGb() != null) {
                systemDisk.setDiskSize(request.getSystemDiskGb().longValue());
            }
            runRequest.setSystemDisk(systemDisk);

            // 数据盘（如果有）
            if (request.getDataDiskGb() != null && request.getDataDiskGb() > 0) {
                DataDisk dataDisk = new DataDisk();
                dataDisk.setDiskSize(request.getDataDiskGb().longValue());
                runRequest.setDataDisks(new DataDisk[]{dataDisk});
            }

            // 网络带宽
            InternetAccessible internet = new InternetAccessible();
            if (request.getBandwidthMbps() != null) {
                internet.setInternetMaxBandwidthOut(request.getBandwidthMbps().longValue());
            }
            internet.setInternetChargeType(request.getBandwidthChargeType());
            runRequest.setInternetAccessible(internet);

            // 计费类型
            runRequest.setInstanceChargeType(request.getInstanceChargeType());

            // 可用区
            Placement placement = new Placement();
            placement.setZone(request.getZone());
            runRequest.setPlacement(placement);

            // 登录设置
            LoginSettings login = new LoginSettings();
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                login.setPassword(request.getPassword());
            }
            if (request.getKeyIds() != null && !request.getKeyIds().isEmpty()) {
                login.setKeyIds(request.getKeyIds().toArray(new String[0]));
            }
            runRequest.setLoginSettings(login);

            // 安全组（如果有）
            if (request.getSecurityGroupIds() != null && !request.getSecurityGroupIds().isEmpty()) {
                runRequest.setSecurityGroupIds(request.getSecurityGroupIds().toArray(new String[0]));
            }

            // 用户数据（可选）
            // 前端输入纯文本脚本，这里统一转为 Base64 后再传腾讯云 API。
            if (request.getUserData() != null && !request.getUserData().trim().isEmpty()) {
                String userDataBase64 = Base64.getEncoder()
                        .encodeToString(request.getUserData().getBytes(StandardCharsets.UTF_8));
                runRequest.setUserData(userDataBase64);
            }

            applyActionTimer(runRequest, request);

            RunInstancesResponse response = client.RunInstances(runRequest);
            if (response.getInstanceIdSet() != null && response.getInstanceIdSet().length > 0) {
                return response.getInstanceIdSet()[0];
            } else {
                throw new IllegalStateException("No instance ID returned from Tencent Cloud.");
            }
        } catch (TencentCloudSDKException ex) {
            throw new IllegalStateException("Failed to create instance.", ex);
        }
    }

    private static void applyActionTimer(RunInstancesRequest runRequest, InstanceCreateRequest request) {
        if (!Boolean.TRUE.equals(request.getScheduledDestroy())) {
            return;
        }
        String actionTime = normalizeActionTime(request.getScheduledDestroyTime());
        if (actionTime == null) {
            throw new IllegalArgumentException("Scheduled destroy time is required when scheduled destroy is enabled.");
        }

        ActionTimer actionTimer = new ActionTimer();
        actionTimer.setTimerAction("TerminateInstances");
        actionTimer.setActionTime(actionTime);
        runRequest.setActionTimer(actionTimer);
    }

    private static Long normalizeInstanceCount(Integer instanceCount) {
        if (instanceCount == null || instanceCount < 1) {
            return 1L;
        }
        return instanceCount.longValue();
    }

    private static String normalizeActionTime(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String value = raw.trim();

        // 1) 已是 ISO8601 UTC（例如 2026-02-26T00:30:00Z）
        try {
            Instant instant = Instant.parse(value).truncatedTo(ChronoUnit.SECONDS);
            return DateTimeFormatter.ISO_INSTANT.format(instant);
        } catch (DateTimeParseException ignored) {
            // ignore
        }

        // 2) 带时区偏移（例如 2026-02-26T08:30:00+08:00）
        try {
            Instant instant = ZonedDateTime.parse(value).toInstant().truncatedTo(ChronoUnit.SECONDS);
            return DateTimeFormatter.ISO_INSTANT.format(instant);
        } catch (DateTimeParseException ignored) {
            // ignore
        }

        // 3) 本地时间（按服务器时区解释）
        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd HH:mm"
        };
        for (String pattern : patterns) {
            try {
                LocalDateTime dt = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(pattern));
                Instant instant = dt.atZone(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.SECONDS);
                return DateTimeFormatter.ISO_INSTANT.format(instant);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        throw new IllegalArgumentException("Invalid scheduled destroy time format: " + raw);
    }
}