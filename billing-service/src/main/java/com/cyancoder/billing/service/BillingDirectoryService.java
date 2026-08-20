package com.cyancoder.billing.service;

import com.cyancoder.billing.api.BillingContracts.*;
import com.cyancoder.billing.model.BillingIdempotencyEntity;
import com.cyancoder.billing.model.PlanEntity;
import com.cyancoder.billing.model.TenantSubscriptionEntity;
import com.cyancoder.billing.model.TenantUsageCounterEntity;
import com.cyancoder.billing.repository.BillingIdempotencyRepository;
import com.cyancoder.billing.repository.PlanRepository;
import com.cyancoder.billing.repository.TenantSubscriptionRepository;
import com.cyancoder.billing.repository.TenantUsageCounterRepository;
import com.cyancoder.billing.security.BillingSecurity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class BillingDirectoryService {
    private final PlanRepository plans;
    private final TenantSubscriptionRepository subscriptions;
    private final BillingIdempotencyRepository idempotency;
    private final TenantUsageCounterRepository usageCounters;
    private final TenantMembershipClient memberships;
    private final BillingSecurity security;
    private final ObjectMapper mapper;

    public BillingDirectoryService(PlanRepository plans, TenantSubscriptionRepository subscriptions, BillingIdempotencyRepository idempotency,
                                   TenantUsageCounterRepository usageCounters, TenantMembershipClient memberships, BillingSecurity security, ObjectMapper mapper) {
        this.plans = plans; this.subscriptions = subscriptions; this.idempotency = idempotency; this.usageCounters = usageCounters; this.memberships = memberships; this.security = security; this.mapper = mapper;
    }

    @Transactional
    public void incrementUsage(String tenantKey, UsageIncrementRequest request) {
        long delta = request.delta() == null ? 1L : request.delta();
        usageCounters.increment(tenantKey, request.metricKey(), delta, Instant.now());
    }

    private Map<String, Long> usage(String tenantKey) {
        Map<String, Long> values = new HashMap<>();
        for (TenantUsageCounterEntity entity : usageCounters.findByIdTenantKey(tenantKey)) values.put(entity.getId().getMetricKey(), entity.getCounterValue());
        return values;
    }

    public List<PlanSummary> listPlans() { return plans.findByActiveTrueOrderByDisplayNameAsc().stream().map(this::planSummary).toList(); }

    @Transactional
    public PlanSummary upsertPlan(PlanUpsertRequest request) {
        security.requirePlatformAdmin();
        PlanEntity entity = plans.findById(request.planKey()).orElseGet(PlanEntity::new);
        entity.setPlanKey(request.planKey()); entity.setDisplayName(request.displayName().trim()); entity.setDescription(request.description());
        entity.setBillingMode(request.billingMode()); entity.setActive(request.active());
        entity.setFeaturesJson(write(request.features() == null ? List.of() : request.features().stream().filter(v -> v != null && !v.isBlank()).distinct().toList()));
        entity.setLimitsJson(write(request.limits() == null ? Map.of() : request.limits()));
        return planSummary(plans.save(entity));
    }

    public SubscriptionSummary subscriptionForCurrentUser(String tenantKey) {
        memberships.require(tenantKey, security.username());
        return subscription(tenantKey);
    }

    public BillingEntitlements internalEntitlements(String tenantKey) {
        SubscriptionSummary value = subscription(tenantKey);
        return new BillingEntitlements(value.planKey(), value.status(), value.features(), value.limits());
    }

    @Transactional
    public SubscriptionSummary change(String tenantKey, ChangeSubscriptionRequest request, String idempotencyKey) {
        memberships.require(tenantKey, security.username());
        return changeOwned(tenantKey, request, idempotencyKey, security.username());
    }

    @Transactional
    public SubscriptionSummary internalChange(String tenantKey, ChangeSubscriptionRequest request, String idempotencyKey) {
        return changeOwned(tenantKey, request, idempotencyKey, "internal-provisioning");
    }

    private SubscriptionSummary changeOwned(String tenantKey, ChangeSubscriptionRequest request, String idempotencyKey, String actor) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("Idempotency-Key is required");
        String recordId = actor + "|subscription-change|" + idempotencyKey.trim();
        if (idempotency.existsById(recordId)) return subscription(tenantKey);
        PlanEntity plan = plans.findById(request.planKey()).filter(PlanEntity::isActive).orElseThrow(NoSuchElementException::new);
        if (!"FREE".equals(plan.getBillingMode())) throw new IllegalStateException("External billing provider is not configured");
        Instant now = Instant.now();
        TenantSubscriptionEntity entity = subscriptions.findById(tenantKey).orElseGet(TenantSubscriptionEntity::new);
        entity.setTenantKey(tenantKey); entity.setPlanKey(plan.getPlanKey()); entity.setStatus("ACTIVE");
        if (entity.getStartedAt() == null) entity.setStartedAt(now);
        entity.setUpdatedAt(now); entity.setRenewsAt(null); subscriptions.save(entity);
        BillingIdempotencyEntity record = new BillingIdempotencyEntity(); record.setRecordId(recordId); record.setTenantKey(tenantKey); record.setCreatedAt(now); idempotency.save(record);
        return subscription(tenantKey);
    }

    private SubscriptionSummary subscription(String tenantKey) {
        TenantSubscriptionEntity entity = subscriptions.findById(tenantKey).orElse(null);
        if (entity == null) return SubscriptionSummary.none(tenantKey);
        PlanEntity plan = plans.findById(entity.getPlanKey()).orElse(null);
        if (plan == null) return new SubscriptionSummary(tenantKey, entity.getPlanKey(), "INVALID", entity.getStartedAt(), entity.getRenewsAt(), List.of(), Map.of(), "NOT_CONFIGURED", usage(tenantKey));
        return new SubscriptionSummary(tenantKey, plan.getPlanKey(), entity.getStatus(), entity.getStartedAt(), entity.getRenewsAt(), features(plan), limits(plan), "FREE".equals(plan.getBillingMode()) ? "NOT_REQUIRED" : "NOT_CONFIGURED", usage(tenantKey));
    }

    private PlanSummary planSummary(PlanEntity plan) { return new PlanSummary(plan.getPlanKey(), plan.getDisplayName(), plan.getDescription(), plan.getBillingMode(), plan.isActive(), features(plan), limits(plan)); }
    private List<String> features(PlanEntity plan) { return read(plan.getFeaturesJson(), new TypeReference<List<String>>() {}, List.of()); }
    private Map<String, Object> limits(PlanEntity plan) { return read(plan.getLimitsJson(), new TypeReference<Map<String, Object>>() {}, Map.of()); }
    private String write(Object value) { try { return mapper.writeValueAsString(value); } catch (Exception error) { throw new IllegalArgumentException("Value cannot be serialized", error); } }
    private <T> T read(String value, TypeReference<T> type, T fallback) { try { return value == null || value.isBlank() ? fallback : mapper.readValue(value, type); } catch (Exception error) { return fallback; } }
}
