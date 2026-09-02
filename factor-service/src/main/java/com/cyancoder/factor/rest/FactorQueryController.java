package com.cyancoder.factor.rest;


import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.model.FactorFilterModel;
import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.model.PageableModel;
import com.cyancoder.factor.service.FactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.core.oidc.OidcIdToken;
//import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.data.domain.Page;
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
    private  final FactorService factorService;


    @GetMapping()
    public List<FactorModel> getFactor(
            @RequestParam String companyId,
            @RequestParam String codeFrom,
            @RequestParam String codeTo,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String factorId) throws java.text.ParseException {

        return factorService.filterFactors(
                companyId.isEmpty() ? null : companyId,
                codeFrom.isEmpty() ? null : codeFrom,
                codeTo.isEmpty() ? null : codeTo,
                fromDate.isEmpty() ? null : fromDate,
                toDate.isEmpty() ? null : toDate,
                factorId.isEmpty() ? null : factorId);
    }


    @GetMapping("getFactorsPaginated/byFilter")
    public Page<FactorEntity> getFactorsByBuyer(FactorFilterModel filter, PageableModel pageableModel){
        return factorService.getFactorByFilter(filter, pageableModel);
    }


}
