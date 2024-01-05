package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service;

import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest.EconomicCodeClientController;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.service.CompanyClientService;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.CompanyModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.eco_code.EconomicCodeRequestDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.eco_code.EconomicCodeResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EconomicCodeService {

    private final EconomicCodeClientController economicCodeClientController;
    private final CompanyClientService companyClientService;


    public EconomicCodeResponseModel getFiscalInformation(String uniqueCode, String companyId, String nationalCode) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        CompanyModel companyModel = companyClientService.getCompany(companyId,uniqueCode);

        Random rnd = new Random();
        Header header = new Header("2023-cyanbusiness-eco-2320011"+ rnd.nextInt(10));

        EconomicCodeRequestDataModel data = EconomicCodeRequestDataModel.builder().economicCode(nationalCode).build();
        RequestModel body = new RequestModel(header, "GET_ECONOMIC_CODE_INFORMATION", data, companyModel.getPk(uniqueCode));

        log.info("header: {}", header);
        log.info("body: {}", body);

        try {
            return new EconomicCodeResponseModel(economicCodeClientController.getEconomicCode(
                    header.getContentType(),
                    header.getString("requestTraceId"),
                    header.getString("timestamp"),
                    body));
        } catch (Exception e) {
            return new EconomicCodeResponseModel(0, e.getMessage());
        }
    }






}
