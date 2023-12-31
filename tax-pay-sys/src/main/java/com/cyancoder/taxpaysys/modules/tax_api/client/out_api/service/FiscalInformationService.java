package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service;

import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest.FiscalInformationClientController;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.fiscal_info.FiscalInfoRequestDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResponseModel;
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
public class FiscalInformationService {

    private final FiscalInformationClientController fiscalInformationClientController;

    private final AuthService authService;

    public ResponseModel getFiscalInformation() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        Random rnd = new Random();
        Header header = new Header("2023-cyanbusiness-fin-2320011"+ rnd.nextInt(10));
        if (header.getString("Authorization") == "")
            authService.setTokenInHeader(header, SellerUser.cyan);

        FiscalInfoRequestDataModel data = FiscalInfoRequestDataModel.builder().build();
        RequestModel body = new RequestModel(header, "GET_FISCAL_INFORMATION", data,SellerUser.cyan);

        log.info("header: {}", header);
        log.info("body: {}", body);

        try {
            return new ResponseModel(fiscalInformationClientController.getFiscalInformation(
                    header.getContentType(),
                    header.getString("requestTraceId"),
                    header.getString("timestamp"),
                    "Bearer "+header.getString("Authorization"),
                    body));
        } catch (Exception e) {
            return new ResponseModel(0, e.getMessage());
        }
    }






}
