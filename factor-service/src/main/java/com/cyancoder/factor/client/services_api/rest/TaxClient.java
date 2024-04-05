package com.cyancoder.factor.client.services_api.rest;

import com.cyancoder.factor.client.services_api.entity.FactorTaxEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "tax-service")
public interface TaxClient {
    @GetMapping(value = "/v2/api/tax-service/inquiry/get-references")
    List<FactorTaxEntity> getReferences(
            @RequestHeader("Authorization") String token,
            @RequestHeader("UniqueCode") String UniqueCode,
            @RequestParam String companyId,
            @RequestParam String factorId);
}
