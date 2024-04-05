package com.cyancoder.taxpaysys.modules.transfer.config;

import com.cyancoder.taxpaysys.modules.transfer.dto.PriorityLevel;
import com.cyancoder.taxpaysys.modules.transfer.http.HttpRequestSender;
import com.cyancoder.taxpaysys.modules.transfer.http.OkHttpRequestSender;
import com.cyancoder.taxpaysys.modules.transfer.impl.normalize.ObjectNormalizer;
import com.cyancoder.taxpaysys.modules.transfer.interfaces.Encrypter;
import com.cyancoder.taxpaysys.modules.transfer.interfaces.Normalizer;
import com.cyancoder.taxpaysys.modules.transfer.interfaces.Signatory;

public class ApiConfig {

    private HttpRequestSender httpRequestSender;

    private Normalizer normalizer;

    private Encrypter encrypter;

    private Signatory signatory;

    private String baseUrl;

    private String apiVersion;

    private PriorityLevel priorityLevel;

    public ApiConfig() {
        this.httpRequestSender = new OkHttpRequestSender();
        this.normalizer = new ObjectNormalizer();
        this.baseUrl = "https://tp.tax.gov.ir/req/api/self-tsp";
        this.apiVersion = null;
        this.priorityLevel = PriorityLevel.NORMAL;
    }

    public ApiConfig httpRequestSender(HttpRequestSender httpRequestSender) {
        this.httpRequestSender = httpRequestSender;
        return this;
    }

    public ApiConfig normalizer(Normalizer normalizer) {
        this.normalizer = normalizer;
        return this;
    }

    public ApiConfig encrypter(Encrypter encrypter) {
        this.encrypter = encrypter;
        return this;
    }

    public ApiConfig signatory(Signatory signatory) {
        this.signatory = signatory;
        return this;
    }

    public ApiConfig baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public ApiConfig apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public ApiConfig priorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
        return this;
    }

    public HttpRequestSender getHttpRequestSender() {
        return httpRequestSender;
    }

    public Normalizer getNormalizer() {
        return normalizer;
    }

    public Encrypter getEncrypter() {
        return encrypter;
    }

    public Signatory getSignatory() {
        return signatory;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }
}
