package com.cyancoder.payment.service;

import com.cyancoder.payment.config.PaymentServiceProperties;
import com.cyancoder.payment.domain.PaymentTransactionStatus;
import com.cyancoder.payment.dto.PaymentCallbackResponse;
import com.cyancoder.payment.dto.PaymentInitiationRequest;
import com.cyancoder.payment.dto.PaymentInitiationResponse;
import com.cyancoder.payment.dto.PaymentTransactionResponse;
import com.cyancoder.payment.entity.PaymentMethodEntity;
import com.cyancoder.payment.entity.PaymentTransactionEntity;
import com.cyancoder.payment.provider.PaymentProviderContext;
import com.cyancoder.payment.provider.PaymentProviderInitResult;
import com.cyancoder.payment.provider.PaymentProviderRegistry;
import com.cyancoder.payment.provider.PaymentProviderStrategy;
import com.cyancoder.payment.provider.PaymentProviderVerificationResult;
import com.cyancoder.payment.provider.PaymentVerificationContext;
import com.cyancoder.payment.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentTransactionService {
    private final PaymentTransactionRepository repository;
    private final PaymentMethodAdminService paymentMethodAdminService;
    private final PaymentProviderRegistry providerRegistry;
    private final PaymentJsonService jsonService;
    private final PaymentServiceProperties properties;

    public PaymentTransactionService(PaymentTransactionRepository repository, PaymentMethodAdminService paymentMethodAdminService, PaymentProviderRegistry providerRegistry, PaymentJsonService jsonService, PaymentServiceProperties properties) {
        this.repository = repository;
        this.paymentMethodAdminService = paymentMethodAdminService;
        this.providerRegistry = providerRegistry;
        this.jsonService = jsonService;
        this.properties = properties;
    }

    @Transactional
    public PaymentInitiationResponse initiate(PaymentInitiationRequest request) {
        PaymentMethodEntity method = paymentMethodAdminService.findEntity(request.paymentMethodKey());
        validateMethodAvailability(method, request.currency(), request.amount());
        PaymentTransactionEntity transaction = new PaymentTransactionEntity();
        transaction.setTransactionKey("pay-" + UUID.randomUUID());
        transaction.setPaymentMethodKey(method.getMethodKey());
        transaction.setProviderCode(method.getProviderCode());
        transaction.setStatus(PaymentTransactionStatus.CREATED);
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency());
        transaction.setOrderKey(request.orderKey());
        transaction.setInvoiceKey(request.invoiceKey());
        transaction.setCustomerKey(request.customerKey());
        transaction.setRelatedService(request.relatedService());
        transaction.setRelatedEntityType(request.relatedEntityType());
        transaction.setRelatedEntityKey(request.relatedEntityKey());
        transaction.setDescription(request.description());
        transaction.setCallbackUrl(request.callbackUrl());
        transaction.setSuccessUrl(request.successUrl());
        transaction.setFailureUrl(request.failureUrl());
        transaction.setMetaDataJson(jsonService.write(request.metaData()));
        PaymentProviderStrategy provider = providerRegistry.get(method.getProviderCode());
        Map<String, Object> configuration = jsonService.readObjectMap(method.getConfigurationJson());
        PaymentProviderInitResult initResult = provider.initiate(new PaymentProviderContext(method, transaction, configuration, properties.getPublicBaseUrl()));
        transaction.setStatus(PaymentTransactionStatus.PENDING_CALLBACK);
        transaction.setProviderReferenceId(initResult.providerReferenceId());
        transaction.setExternalPaymentId(initResult.externalPaymentId());
        transaction.setPaymentToken(initResult.paymentToken());
        transaction.setPaymentUrl(initResult.paymentUrl());
        transaction.setProviderResponseJson(jsonService.write(Map.of(
                "message", initResult.providerResponseMessage(),
                "clientToken", initResult.clientToken() == null ? "" : initResult.clientToken()
        )));
        repository.save(transaction);
        return new PaymentInitiationResponse(
                transaction.getTransactionKey(),
                transaction.getStatus(),
                transaction.getPaymentMethodKey(),
                transaction.getProviderCode(),
                initResult.paymentUrl(),
                initResult.clientToken(),
                initResult.providerReferenceId(),
                initResult.externalPaymentId(),
                initResult.httpMethod(),
                initResult.formFields(),
                properties.getPublicBaseUrl() + "/public/payment/callback/" + transaction.getProviderCode().name().toLowerCase() + "/" + transaction.getTransactionKey()
        );
    }

    @Transactional
    public PaymentTransactionResponse verify(String transactionKey, Map<String, String> payload) {
        PaymentTransactionEntity transaction = findEntity(transactionKey);
        PaymentMethodEntity method = paymentMethodAdminService.findEntity(transaction.getPaymentMethodKey());
        PaymentProviderStrategy provider = providerRegistry.get(transaction.getProviderCode());
        PaymentProviderVerificationResult result = provider.verify(new PaymentVerificationContext(method, transaction, jsonService.readObjectMap(method.getConfigurationJson()), payload == null ? Map.of() : payload));
        applyVerificationResult(transaction, payload, result);
        return toResponse(repository.save(transaction));
    }

    @Transactional
    public PaymentCallbackResponse handleCallback(String providerCode, String transactionKey, Map<String, String> payload) {
        PaymentTransactionEntity transaction = findEntity(transactionKey);
        if (!transaction.getProviderCode().name().equalsIgnoreCase(providerCode)) {
            throw new IllegalArgumentException("Provider mismatch for transaction " + transactionKey);
        }
        PaymentMethodEntity method = paymentMethodAdminService.findEntity(transaction.getPaymentMethodKey());
        PaymentProviderStrategy provider = providerRegistry.get(transaction.getProviderCode());
        PaymentProviderVerificationResult result = provider.handleCallback(new PaymentVerificationContext(method, transaction, jsonService.readObjectMap(method.getConfigurationJson()), payload));
        applyVerificationResult(transaction, payload, result);
        repository.save(transaction);
        String redirectUrl = result.successful() ? transaction.getSuccessUrl() : transaction.getFailureUrl();
        return new PaymentCallbackResponse(
                transaction.getTransactionKey(),
                transaction.getProviderCode(),
                transaction.getStatus(),
                result.successful(),
                result.message(),
                redirectUrl,
                payload
        );
    }

    public PaymentTransactionResponse get(String transactionKey) {
        return toResponse(findEntity(transactionKey));
    }

    public List<PaymentTransactionResponse> list(String orderKey, PaymentTransactionStatus status) {
        if (orderKey != null && !orderKey.isBlank()) {
            return repository.findByOrderKeyOrderByCreatedAtDesc(orderKey).stream().map(this::toResponse).toList();
        }
        if (status != null) {
            return repository.findByStatusOrderByCreatedAtDesc(status).stream().map(this::toResponse).toList();
        }
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private void applyVerificationResult(PaymentTransactionEntity transaction, Map<String, String> payload, PaymentProviderVerificationResult result) {
        transaction.setStatus(result.successful() ? PaymentTransactionStatus.VERIFIED : PaymentTransactionStatus.FAILED);
        transaction.setProviderReferenceId(result.providerReferenceId());
        transaction.setExternalPaymentId(result.externalPaymentId());
        transaction.setPaymentToken(result.paymentToken());
        transaction.setMaskedCardNumber(result.maskedCardNumber());
        transaction.setVerificationMessage(result.message());
        transaction.setCallbackPayloadJson(jsonService.write(payload == null ? Map.of() : payload));
    }

    private void validateMethodAvailability(PaymentMethodEntity method, String currency, BigDecimal amount) {
        if (!method.isEnabled() || !method.isActive()) {
            throw new IllegalArgumentException("Payment method is not active: " + method.getMethodKey());
        }
        if (!jsonService.readStringSet(method.getSupportedCurrenciesJson()).contains(currency)) {
            throw new IllegalArgumentException("Currency " + currency + " is not supported by " + method.getMethodKey());
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }

    private PaymentTransactionEntity findEntity(String transactionKey) {
        return repository.findByTransactionKey(transactionKey)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found: " + transactionKey));
    }

    private PaymentTransactionResponse toResponse(PaymentTransactionEntity entity) {
        return new PaymentTransactionResponse(
                entity.getTransactionKey(),
                entity.getPaymentMethodKey(),
                entity.getProviderCode(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getOrderKey(),
                entity.getInvoiceKey(),
                entity.getCustomerKey(),
                entity.getRelatedService(),
                entity.getRelatedEntityType(),
                entity.getRelatedEntityKey(),
                entity.getDescription(),
                entity.getPaymentUrl(),
                entity.getCallbackUrl(),
                entity.getSuccessUrl(),
                entity.getFailureUrl(),
                entity.getProviderReferenceId(),
                entity.getExternalPaymentId(),
                entity.getPaymentToken(),
                entity.getMaskedCardNumber(),
                entity.getVerificationMessage(),
                jsonService.readStringMap(entity.getMetaDataJson()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
