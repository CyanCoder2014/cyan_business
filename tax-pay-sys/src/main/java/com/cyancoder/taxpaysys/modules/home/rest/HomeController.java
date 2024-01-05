package com.cyancoder.taxpaysys.modules.home.rest;


import com.cyancoder.taxpaysys.modules.home.client.ExternalFeignClient;
import com.cyancoder.taxpaysys.modules.home.dto.res.TestEncryptResponseModel;
import com.cyancoder.taxpaysys.modules.home.dto.res.TestResponseModel;
import com.cyancoder.taxpaysys.modules.home.service.ExternalFeignClientAsync;
import com.cyancoder.taxpaysys.modules.home.service.ExternalService;
import com.cyancoder.taxpaysys.modules.home.service.TicketService;
import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.auth.OAuth2AuthenticationToken;
import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service.ServerInformationService;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.service.FactorClientService;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestFactorModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.auth.AuthRequestDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info.ServerInfoResponseModel;
import com.cyancoder.taxpaysys.modules.tax_api.repository.FactorTaxRepository;
import com.cyancoder.taxpaysys.util.CryptoUtils;
import com.cyancoder.taxpaysys.util.Encryption;
import com.cyancoder.taxpaysys.util.KeyUtil;
import com.cyancoder.taxpaysys.util.SignText;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.HeaderParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v2/api/tax-service")
@RequiredArgsConstructor
public class HomeController {

    private final ExternalFeignClientAsync externalFeignClientAsync;

    private final ExternalService externalService;


    private final ServerInformationService serverInformationService;

    private final FactorClientService factorClientService;

    @GetMapping
    public ResponseEntity<String> home() throws InterruptedException {
        int rndNum = (int) (Math.random()*5000);
        String welcomeMessage = "rest with delay: "+rndNum;
//        ticketService.addTicket();
        Thread.sleep(rndNum);

        return new ResponseEntity<String>(welcomeMessage, HttpStatusCode.valueOf(200));
    }

    @GetMapping("test")
    public  List<Object> test(){

        return externalService.getAllRecords();
    }


    @GetMapping("sign")
    public  TestResponseModel sign() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        Header header = new Header("1231232");
        AuthRequestDataModel data = AuthRequestDataModel.builder().username("A119W2").build();
        RequestModel body = new RequestModel(header, "GET_TOKEN", data, "");

        String normalJsonStr = CryptoUtils.normalJson(body.getPacket(), header);
        String ex = SignText.getSignedText(normalJsonStr, "SHA256WITHRSA", KeyUtil.getPrivateKey(SellerUser.cyan));

    return new TestResponseModel(
            header,
            body,
            normalJsonStr,
            ex);
    }



    @GetMapping("encrypt")
    public  TestEncryptResponseModel encrypt() throws Exception {

        Header header = new Header("1231232");
        AuthRequestDataModel data = AuthRequestDataModel.builder().username("A119W2").build();
        RequestModel body = new RequestModel(header, "GET_TOKEN", data,"");

        String normalJsonStr = CryptoUtils.normalJson(body.getPacket(), header);
        String ex = SignText.getSignedText(normalJsonStr, "SHA256WITHRSA", KeyUtil.getPrivateKey(SellerUser.cyan));





        SecretKey aesKey = Encryption.getAESKey(256);
        byte[] iv = Encryption.getRandomNonce(128);

        byte[] encrypt = Encryption.encrypt(iv,aesKey,iv);

        byte[] xor = Encryption.xor(encrypt,iv);




        ServerInfoResponseModel response = serverInformationService.getServerInformation(KeyUtil.getStringPrivateKey(SellerUser.cyan));
        String serverKey = response.successResponse != null ? response.successResponse.result.data.publicKeys[0].key  : null;

        String encryptString = Encryption.encrypt("",KeyUtil.getServerPublicKey(serverKey));


    return new TestEncryptResponseModel(
            serverKey,
            aesKey,
            iv,
            encrypt,
            xor
//            header,
//            body,
//            normalJsonStr,
//            ex
    );
    }



    @GetMapping("/get-factors")
    @RolesAllowed({ "ROLE_cyan-business_user" })
//    @PreAuthorize("hasAuthority('user')")
    public Object getFactors(@RequestHeader("UniqueCode")String uniqueCode,
                             @RequestBody RequestFactorModel requestFactorModel) throws ParseException {


//        String date_string1 = "2023-05-01";
//        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
////        formatter1.setTimeZone(TimeZone.getTimeZone("teh"));
//        Date date1 = formatter1.parse(date_string1);
//
//        String date_string2 = "2023-12-20";
//        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
//        Date date2 = formatter2.parse(date_string2);
//
//
        return factorClientService.getFactors(requestFactorModel);
////        return factorRepository.findAll(Pageable.ofSize(12)
//////                .getSortOr(Sort.by(Sort.Direction.DESC, "id"))
////        );

    }

}


