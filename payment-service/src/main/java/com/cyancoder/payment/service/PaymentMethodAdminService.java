package com.cyancoder.payment.service;

import com.cyancoder.payment.dto.AvailablePaymentMethodResponse;
import com.cyancoder.payment.dto.PaymentMethodAdminResponse;
import com.cyancoder.payment.dto.PaymentMethodRequest;
import com.cyancoder.payment.entity.PaymentMethodEntity;
import com.cyancoder.payment.provider.PaymentProviderRegistry;
import com.cyancoder.payment.repository.PaymentMethodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentMethodAdminService {
    private final PaymentMethodRepository repository;
    private final PaymentProviderRegistry registry;
    private final PaymentJsonService jsonService;

    public PaymentMethodAdminService(PaymentMethodRepository repository, PaymentProviderRegistry registry, PaymentJsonService jsonService) {
        this.repository = repository;
        this.registry = registry;
        this.jsonService = jsonService;
    }

    @Transactional
    public PaymentMethodAdminResponse create(PaymentMethodRequest request) {
        repository.findByMethodKey(request.methodKey()).ifPresent(existing -> {
            throw new IllegalArgumentException("Payment method already exists: " + request.methodKey());
        });
        PaymentMethodEntity entity = new PaymentMethodEntity();
        apply(entity, request);
        return toAdminResponse(repository.save(entity));
    }

    @Transactional
    public PaymentMethodAdminResponse update(String methodKey, PaymentMethodRequest request) {
        PaymentMethodEntity entity = findEntity(methodKey);
        apply(entity, request);
        return toAdminResponse(repository.save(entity));
    }

    public List<PaymentMethodAdminResponse> list() {
        return repository.findAll().stream().map(this::toAdminResponse).toList();
    }

    public List<AvailablePaymentMethodResponse> listAvailable() {
        return repository.findByEnabledTrueAndActiveTrueOrderByPriorityOrderAscDisplayNameAsc()
                .stream()
                .map(this::toAvailableResponse)
                .toList();
    }

    public PaymentMethodAdminResponse get(String methodKey) {
        return toAdminResponse(findEntity(methodKey));
    }

    @Transactional
    public void delete(String methodKey) {
        repository.delete(findEntity(methodKey));
    }

    public Optional<PaymentMethodEntity> findOptionalEntity(String methodKey) {
        return repository.findByMethodKey(methodKey);
    }

    public PaymentMethodEntity findEntity(String methodKey) {
        return repository.findByMethodKey(methodKey)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + methodKey));
    }

    private void apply(PaymentMethodEntity entity, PaymentMethodRequest request) {
        registry.get(request.providerCode()).validateConfiguration(request.configuration());
        entity.setMethodKey(request.methodKey());
        entity.setDisplayName(request.displayName());
        entity.setProviderCode(request.providerCode());
        entity.setRegion(request.region());
        entity.setFlowType(request.flowType());
        entity.setEnabled(request.enabled());
        entity.setActive(request.active());
        entity.setPriorityOrder(request.priorityOrder());
        entity.setSupportedCurrenciesJson(jsonService.write(request.supportedCurrencies()));
        entity.setConfigurationJson(jsonService.write(request.configuration()));
        entity.setDescription(request.description());
    }

    private PaymentMethodAdminResponse toAdminResponse(PaymentMethodEntity entity) {
        return new PaymentMethodAdminResponse(
                entity.getMethodKey(),
                entity.getDisplayName(),
                entity.getProviderCode(),
                entity.getRegion(),
                entity.getFlowType(),
                entity.isEnabled(),
                entity.isActive(),
                entity.getPriorityOrder(),
                jsonService.readStringSet(entity.getSupportedCurrenciesJson()),
                jsonService.readObjectMap(entity.getConfigurationJson()),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AvailablePaymentMethodResponse toAvailableResponse(PaymentMethodEntity entity) {
        return new AvailablePaymentMethodResponse(
                entity.getMethodKey(),
                entity.getDisplayName(),
                entity.getProviderCode(),
                entity.getRegion(),
                entity.getFlowType(),
                entity.getPriorityOrder(),
                jsonService.readStringSet(entity.getSupportedCurrenciesJson()),
                entity.getDescription()
        );
    }
}
