package com.cyancoder.taxpaysys.modules.tax_api.client.services_api.service;

import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.ResponseModel;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.rest.FactorClient;
import com.cyancoder.taxpaysys.modules.tax_api.model.FactorModel;
import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.auth.OAuth2AuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final WebClient.Builder webClientBuilder;

    public Object getFactors()  {

//        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
//        Map<String, Object> attributes = null;
//        if (authToken instanceof OAuth2AuthenticationToken) {
//            attributes = ((OAuth2AuthenticationToken) authToken).getPrincipal().getAttributes();
//        } else if (authToken instanceof JwtAuthenticationToken) {
//            attributes = ((JwtAuthenticationToken) authToken).getTokenAttributes();
//
//        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthenticationToken oauthToken = (JwtAuthenticationToken) authentication;

        return factorClient.getItemsT("Bearer "+oauthToken.getToken().getTokenValue());
    }


    public List<FactorModel> findByCodeBetween(String codeFrom, String codeTo) {

        try {
            return new ResponseModel(factorClient.getItems());
        } catch (Exception e) {
            throw e;////////////////////
        }
    }


    public List<FactorModel> findByCreatedOnBetween(Date fromDate, Date toDate)  {

        try {
            return new ResponseModel(factorClient.getItems());
        } catch (Exception e) {
            throw e;////////////////////
        }
    }
  public List<FactorModel> findByCode(String factorCode)  {

        try {
            return new ResponseModel(factorClient.getItems());
        } catch (Exception e) {
            throw e;////////////////////
        }
    }
 public List<FactorModel> findById(String factorId)  {

        try {
            return new ResponseModel(factorClient.getItems());
        } catch (Exception e) {
            throw e;////////////////////
        }
    }








}
