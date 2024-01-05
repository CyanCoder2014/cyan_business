package com.cyancoder.factor.rest;


import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.query.FilterFactorQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v2/api/factor-service/factors")
@RequiredArgsConstructor
@Slf4j
public class FactorQueryController {


    private  final Environment env;
    private  final QueryGateway queryGateway;


    @GetMapping
    public List<FactorModel> getFactor(
            @RequestParam String companyId,
            @RequestParam String codeFrom,
            @RequestParam String codeTo,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String factorId){

        FilterFactorQuery filterFactorQuery = new FilterFactorQuery();
        filterFactorQuery.setCompanyId(companyId==""?null:companyId);
        filterFactorQuery.setCodeFrom(codeFrom==""?null:codeFrom);
        filterFactorQuery.setCodeTo(codeTo==""?null:codeTo);
        filterFactorQuery.setFromDate(fromDate==""?null:fromDate);
        filterFactorQuery.setToDate(toDate==""?null:toDate);
        filterFactorQuery.setFactorId(factorId==""?null:factorId);

        List<FactorModel> factors = queryGateway.query(filterFactorQuery,
                ResponseTypes.multipleInstancesOf(FactorModel.class)).join();

        return factors;
    }


}
