package com.cyancoder.factor.client.services_api.service;

import com.cyancoder.factor.client.services_api.entity.FactorTaxEntity;
import com.cyancoder.factor.client.services_api.rest.TaxClient;
import com.cyancoder.factor.config.OauthToken;
import com.cyancoder.factor.model.request.RequestTaxModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxClientService {
    private final TaxClient taxClient;
    private final OauthToken oauthToken;

    public List<FactorTaxEntity> getReferences(RequestTaxModel requestFactorModel) {
        return taxClient.getReferences(
                "Bearer " + oauthToken.getToken(),
                requestFactorModel.getUniqueCode(),
                requestFactorModel.getCompanyId(),
                requestFactorModel.getFactorId());
    }
}
