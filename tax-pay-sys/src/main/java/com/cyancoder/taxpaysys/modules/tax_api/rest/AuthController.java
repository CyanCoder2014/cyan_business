package com.cyancoder.taxpaysys.modules.tax_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.client.service.AuthService;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.auth.AuthResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/v2/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    AuthService authService;


    @GetMapping("/get-token")
    Object getToken(@RequestParam int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        AuthResponseModel response = authService.getToken(sellerEnm);
        return response.successResponse != null ? response.successResponse.result.data  : response;
    }


    @GetMapping("/set-token")
    Object setToken(HttpServletRequest request) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        return authService.setToken();
    }
}
