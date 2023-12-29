package com.cyancoder.factor.rest;


import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.query.FilterFactorQuery;
import lombok.RequiredArgsConstructor;
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
public class FactorQueryController {


    private  final Environment env;
    private  final QueryGateway queryGateway;


    @GetMapping
    public List<FactorModel> getFactor(){

        FilterFactorQuery filterFactorQuery = new FilterFactorQuery();

        List<FactorModel> factors = queryGateway.query(filterFactorQuery,
                ResponseTypes.multipleInstancesOf(FactorModel.class)).join();


        return factors;


    }




    @GetMapping("/userinfo")
    public Object userInfoController() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Object jwt = authentication.getPrincipal();
        return authentication;

    }


}
