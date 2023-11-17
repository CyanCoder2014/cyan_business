package com.cyancoder.taxpaysys.modules.tax_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.client.service.EconomicCodeService;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.eco_code.EconomicCodeResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/v2/api/tax/eco-code")
@Slf4j
public class EconomicCodeController {

    @Autowired
    EconomicCodeService economicCodeService;


    @GetMapping("/get-code")
    Object getFiscalInfo(HttpServletRequest request) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        EconomicCodeResponseModel response = economicCodeService.getFiscalInformation();
        return response.successResponse != null ? response.successResponse.result.data  : response;
    }
}
