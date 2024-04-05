package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service;

import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest.ServerInformationClientController;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.fiscal_info.FiscalInfoRequestDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info.ServerInfoResponseModel;
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
public class ServerInformationService {

    private final ServerInformationClientController serverInformationClientController;


    public ServerInfoResponseModel getServerInformation(String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        Random rnd = new Random();
        Header header = new Header("2023-cyanbusiness-srv-2320011"+ rnd.nextInt(10));

        FiscalInfoRequestDataModel data = FiscalInfoRequestDataModel.builder().build();
        RequestModel body = new RequestModel(header, "GET_SERVER_INFORMATION", data, privateKey);

        log.info("header: {}", header);
        log.info("body: {}", body);

        try {
            return new ServerInfoResponseModel(serverInformationClientController.getServerInformation(
                    header.getContentType(),
                    header.getString("requestTraceId"),
                    header.getString("timestamp"),
                    body));
        } catch (Exception e) {
            return new ServerInfoResponseModel(0, e.getMessage());
        }
    }






}
