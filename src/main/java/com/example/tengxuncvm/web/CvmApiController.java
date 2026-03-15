package com.example.tengxuncvm.web;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

import com.example.tengxuncvm.service.CredentialStore;
import com.example.tengxuncvm.service.CvmPriceService;
import com.example.tengxuncvm.service.ImageService;
import com.example.tengxuncvm.service.InstanceService;
import com.example.tengxuncvm.service.InstanceTypeService;
import com.example.tengxuncvm.service.RegionService;
import com.example.tengxuncvm.service.ZoneService;
import com.example.tengxuncvm.service.CvmInstanceCreateService;
import com.example.tengxuncvm.service.CvmInstanceOperationService;
import com.example.tengxuncvm.service.CvmInstanceDestroyService;
import com.example.tengxuncvm.service.CvmInstanceRenewService;
import com.example.tengxuncvm.service.BillingService;

import com.example.tengxuncvm.web.InstanceCreateRequest;
import com.example.tengxuncvm.web.InstanceModifyRequest;
import com.example.tengxuncvm.web.BalanceResponse;
import com.example.tengxuncvm.web.BillDto;
import com.example.tengxuncvm.web.BillListResponse;

@RestController
@RequestMapping("/api")
public class CvmApiController {

    private final CredentialStore credentialStore;
    private final RegionService regionService;
    private final ZoneService zoneService;
    private final InstanceTypeService instanceTypeService;
    private final ImageService imageService;
    private final CvmPriceService priceService;
    private final InstanceService instanceService;
    private final CvmInstanceCreateService instanceCreateService;
    private final CvmInstanceOperationService instanceOperationService;
    private final CvmInstanceDestroyService instanceDestroyService;
    private final CvmInstanceRenewService instanceRenewService;
    private final BillingService billingService;

    private final Map<String, LoginAttemptCounter> loginCounterByIp = new ConcurrentHashMap<>();
    private static final long WINDOW_MS = 60_000L;       // 1分钟
    private static final long BAN_MS = 10 * 60_000L;     // 10分钟


    public CvmApiController(
            CredentialStore credentialStore,
            RegionService regionService,
            ZoneService zoneService,
            InstanceTypeService instanceTypeService,
            ImageService imageService,
            CvmPriceService priceService,
            InstanceService instanceService,
            CvmInstanceCreateService instanceCreateService,
            CvmInstanceOperationService instanceOperationService,
            CvmInstanceDestroyService instanceDestroyService,
            CvmInstanceRenewService instanceRenewService,
            BillingService billingService
            ) {
        this.credentialStore = credentialStore;
        this.regionService = regionService;
        this.zoneService = zoneService;
        this.instanceTypeService = instanceTypeService;
        this.imageService = imageService;
        this.priceService = priceService;
        this.instanceService = instanceService;
        this.instanceCreateService = instanceCreateService;
        this.instanceOperationService = instanceOperationService;
        this.instanceDestroyService = instanceDestroyService;
        this.instanceRenewService = instanceRenewService;
        this.billingService = billingService;
    }

    @PostMapping("/credentials")
    public Map<String, String> updateCredentials(@RequestBody CredentialsRequest request, HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);
        LoginAttemptCounter counter = loginCounterByIp.computeIfAbsent(clientIp, ip -> new LoginAttemptCounter());

        long now = System.currentTimeMillis();
        counter.prune(now, WINDOW_MS);
        if (counter.isBanned(now)) {
            long waitSec = Math.max(1, (counter.banUntil - now) / 1000);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "该IP已被临时封禁，请" + waitSec + "秒后再试");
        }

        counter.recordAttempt(now);
        try {
            credentialStore.update(request.getSecretId(), request.getSecretKey(), request.getDefaultRegion());
            // 立即校验凭证是否可用（不改现有业务流程，只做一次轻量校验）
            regionService.listRegions();
            counter.reset();

            Map<String, String> response = new HashMap<>();
            response.put("status", "ok");
            return response;
        } catch (Exception ex) {
            counter.recordFailure(now);
            if (counter.failuresInWindow() >= 10 && counter.attemptsInWindow() > 100) {
                counter.banUntil = now + BAN_MS;
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "登录失败过于频繁，该IP已封禁10分钟");
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "访问凭证无效或不可用，请检查 SecretId/SecretKey");
        }
    }

    @GetMapping("/regions")
    public List<String> listRegions() {
        return regionService.listRegions();
    }

    @GetMapping("/zones")
    public List<String> listZones(@RequestParam String region) {
        return zoneService.listZones(resolveRegion(region));
    }

    @GetMapping("/instance-types")
    public List<InstanceTypeDto> listInstanceTypes(@RequestParam String region) {
        return instanceTypeService.listInstanceTypes(resolveRegion(region));
    }

    @GetMapping("/images")
    public List<ImageDto> listImages(@RequestParam String region) {
        return imageService.listImages(resolveRegion(region));
    }

    @PostMapping("/price")
    public PriceResponse quotePrice(@RequestBody PriceRequest request) {
        request.setRegion(resolveRegion(request.getRegion()));
        return priceService.quotePrice(request);
    }

    @GetMapping("/instances")
    public InstanceListResponse listInstances(
            @RequestParam String region,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return instanceService.listInstances(resolveRegion(region), page, size);
    }

    @PostMapping("/instances")
    public Map<String, String> createInstance(@RequestBody InstanceCreateRequest request) {
        String instanceId = instanceCreateService.createInstance(request);
        Map<String, String> response = new HashMap<>();
        response.put("instanceId", instanceId);
        response.put("status", "ok");
        return response;
    }

    @PostMapping("/instances/{instanceId}/start")
    public Map<String, String> startInstance(@PathVariable String instanceId, @RequestParam String region) {
        instanceOperationService.startInstance(resolveRegion(region), instanceId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }

    @PostMapping("/instances/{instanceId}/stop")
    public Map<String, String> stopInstance(@PathVariable String instanceId, @RequestParam String region) {
        instanceOperationService.stopInstance(resolveRegion(region), instanceId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }

    @PostMapping("/instances/{instanceId}/reboot")
    public Map<String, String> rebootInstance(@PathVariable String instanceId, @RequestParam String region) {
        instanceOperationService.rebootInstance(resolveRegion(region), instanceId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }

    @PutMapping("/instances/{instanceId}")
    public Map<String, String> modifyInstance(@PathVariable String instanceId, @RequestParam String region,
                                              @RequestBody InstanceModifyRequest request) {
        instanceOperationService.modifyInstanceAttribute(resolveRegion(region), instanceId, request.getInstanceName());
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }

    @DeleteMapping("/instances/{instanceId}")
    public Map<String, String> destroyInstance(@PathVariable String instanceId, @RequestParam String region) {
        instanceDestroyService.destroyInstance(resolveRegion(region), instanceId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }

    @PostMapping("/instances/{instanceId}/renew")
    public Map<String, String> renewInstance(@PathVariable String instanceId, @RequestParam String region,
                                             @RequestParam int periodMonths) {
        instanceRenewService.renewInstance(resolveRegion(region), instanceId, periodMonths);
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        return response;
    }

    @GetMapping("/billing/balance")
    public BalanceResponse getBalance() {
        return billingService.getAccountBalance();
    }

    @GetMapping("/billing/bills")
    public BillListResponse getBills(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String billingCycle) {
        return billingService.getBills(page, size, billingCycle);
    }

    @GetMapping("/billing/bills/{billId}")
    public BillDto getBillDetail(@PathVariable String billId) {
        return billingService.getBillDetail(billId);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private static class LoginAttemptCounter {
        private final Deque<Long> attempts = new ArrayDeque<>();
        private final Deque<Long> failures = new ArrayDeque<>();
        private volatile long banUntil = 0L;

        void prune(long now, long windowMs) {
            while (!attempts.isEmpty() && now - attempts.peekFirst() > windowMs) {
                attempts.pollFirst();
            }
            while (!failures.isEmpty() && now - failures.peekFirst() > windowMs) {
                failures.pollFirst();
            }
        }

        void recordAttempt(long now) { attempts.addLast(now); }

        void recordFailure(long now) { failures.addLast(now); }

        int attemptsInWindow() { return attempts.size(); }

        int failuresInWindow() { return failures.size(); }

        boolean isBanned(long now) { return now < banUntil; }

        void reset() {
            attempts.clear();
            failures.clear();
            banUntil = 0L;
        }
    }

    private String resolveRegion(String region) {
        if (region == null || region.trim().isEmpty()) {
            return credentialStore.getDefaultRegionOrFallback();
        }
        return region;
    }
}
