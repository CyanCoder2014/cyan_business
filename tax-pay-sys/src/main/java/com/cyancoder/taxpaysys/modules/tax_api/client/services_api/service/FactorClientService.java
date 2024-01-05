package com.cyancoder.taxpaysys.modules.tax_api.client.services_api.service;

import com.cyancoder.taxpaysys.config.OauthToken;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.ResponseModel;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.rest.FactorClient;
import com.cyancoder.taxpaysys.modules.tax_api.model.FactorModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestFactorModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class FactorClientService {

    private final FactorClient factorClient;
    private final OauthToken oauthToken;
//    private final WebClient.Builder webClientBuilder;



    public List<FactorModel> getFactors(RequestFactorModel requestFactorModel) {

        return factorClient.getItems(
                "Bearer "+oauthToken.getToken(),
                requestFactorModel.getCompanyId(),
                requestFactorModel.getCodeFrom(),
                requestFactorModel.getCodeTo(),
                requestFactorModel.getFromDate(),
                requestFactorModel.getToDate(),
                requestFactorModel.getFactorId());
//        try {
//            return new ResponseModel(factorClient.getItems("Bearer "+oauthToken.getToken(),requestFactorModel));
//        } catch (Exception e) {
//            throw e;////////////////////
//        }
    }




}
