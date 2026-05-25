package com.cyancoder.payment.entity;

import com.cyancoder.payment.domain.PaymentProviderCode;
import com.cyancoder.payment.domain.PaymentTransactionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_transaction", indexes = {
        @Index(name = "idx_payment_transaction_key", columnList = "transactionKey", unique = true),
        @Index(name = "idx_payment_related_entity", columnList = "relatedEntityKey"),
        @Index(name = "idx_payment_order_key", columnList = "orderKey")
})
public class PaymentTransactionEntity extends BaseAuditEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String transactionKey;

    @Column(nullable = false, length = 120)
    private String paymentMethodKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentProviderCode providerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentTransactionStatus status;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 12)
    private String currency;

    @Column(length = 120)
    private String orderKey;

    @Column(length = 120)
    private String invoiceKey;

    @Column(length = 120)
    private String customerKey;

    @Column(length = 120)
    private String relatedService;

    @Column(length = 120)
    private String relatedEntityType;

    @Column(length = 120)
    private String relatedEntityKey;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String paymentUrl;

    @Column(length = 255)
    private String callbackUrl;

    @Column(length = 255)
    private String successUrl;

    @Column(length = 255)
    private String failureUrl;

    @Column(length = 255)
    private String providerReferenceId;

    @Column(length = 255)
    private String externalPaymentId;

    @Column(length = 255)
    private String paymentToken;

    @Column(length = 40)
    private String maskedCardNumber;

    @Column(length = 500)
    private String verificationMessage;

    @Column(columnDefinition = "TEXT")
    private String metaDataJson;

    @Column(columnDefinition = "TEXT")
    private String providerRequestJson;

    @Column(columnDefinition = "TEXT")
    private String providerResponseJson;

    @Column(columnDefinition = "TEXT")
    private String callbackPayloadJson;

    public String getTransactionKey() {
        return transactionKey;
    }

    public void setTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
    }

    public String getPaymentMethodKey() {
        return paymentMethodKey;
    }

    public void setPaymentMethodKey(String paymentMethodKey) {
        this.paymentMethodKey = paymentMethodKey;
    }

    public PaymentProviderCode getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(PaymentProviderCode providerCode) {
        this.providerCode = providerCode;
    }

    public PaymentTransactionStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentTransactionStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public String getInvoiceKey() {
        return invoiceKey;
    }

    public void setInvoiceKey(String invoiceKey) {
        this.invoiceKey = invoiceKey;
    }

    public String getCustomerKey() {
        return customerKey;
    }

    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }

    public String getRelatedService() {
        return relatedService;
    }

    public void setRelatedService(String relatedService) {
        this.relatedService = relatedService;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public String getRelatedEntityKey() {
        return relatedEntityKey;
    }

    public void setRelatedEntityKey(String relatedEntityKey) {
        this.relatedEntityKey = relatedEntityKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getFailureUrl() {
        return failureUrl;
    }

    public void setFailureUrl(String failureUrl) {
        this.failureUrl = failureUrl;
    }

    public String getProviderReferenceId() {
        return providerReferenceId;
    }

    public void setProviderReferenceId(String providerReferenceId) {
        this.providerReferenceId = providerReferenceId;
    }

    public String getExternalPaymentId() {
        return externalPaymentId;
    }

    public void setExternalPaymentId(String externalPaymentId) {
        this.externalPaymentId = externalPaymentId;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getVerificationMessage() {
        return verificationMessage;
    }

    public void setVerificationMessage(String verificationMessage) {
        this.verificationMessage = verificationMessage;
    }

    public String getMetaDataJson() {
        return metaDataJson;
    }

    public void setMetaDataJson(String metaDataJson) {
        this.metaDataJson = metaDataJson;
    }

    public String getProviderRequestJson() {
        return providerRequestJson;
    }

    public void setProviderRequestJson(String providerRequestJson) {
        this.providerRequestJson = providerRequestJson;
    }

    public String getProviderResponseJson() {
        return providerResponseJson;
    }

    public void setProviderResponseJson(String providerResponseJson) {
        this.providerResponseJson = providerResponseJson;
    }

    public String getCallbackPayloadJson() {
        return callbackPayloadJson;
    }

    public void setCallbackPayloadJson(String callbackPayloadJson) {
        this.callbackPayloadJson = callbackPayloadJson;
    }
}
