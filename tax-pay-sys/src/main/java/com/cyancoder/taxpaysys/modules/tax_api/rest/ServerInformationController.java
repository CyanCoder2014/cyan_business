package com.cyancoder.taxpaysys.modules.tax_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service.ServerInformationService;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info.ServerInfoResponseModel;
import com.cyancoder.taxpaysys.util.KeyUtil;
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
@RequestMapping("/v2/api/tax/server-info")
@Slf4j
public class ServerInformationController {

    @Autowired
    ServerInformationService serverInformationService;


    @GetMapping("/get-info")
    Object getFiscalInfo(HttpServletRequest request) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        ServerInfoResponseModel response = serverInformationService.getServerInformation(KeyUtil.getStringPrivateKey(SellerUser.cyan));///////////////
        return response.successResponse != null ? response.successResponse.result.data  : response;
    }

}
