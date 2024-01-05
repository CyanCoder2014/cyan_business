package com.cyancoder.taxpaysys.modules.tax_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service.EconomicCodeService;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.eco_code.EconomicCodeResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/v2/api/tax-service/company-data")
@Slf4j
public class EconomicCodeController {

    @Autowired
    EconomicCodeService economicCodeService;


    @GetMapping("/get-info")
    Object getFiscalInfo(@RequestHeader("UniqueCode")String uniqueCode,
                         @RequestParam String companyId,
                         @RequestParam String nationalCode) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        EconomicCodeResponseModel response = economicCodeService.getFiscalInformation(uniqueCode, companyId, nationalCode);
        return response.successResponse != null ? response.successResponse.result.data  : response;
    }
}
