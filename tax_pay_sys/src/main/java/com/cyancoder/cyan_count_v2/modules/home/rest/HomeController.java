package main.java.com.cyancoder.tax_pay_sys_service.modules.home.rest;


import com.cyancoder.tax_pay_sys_service.modules.home.client.ExternalFeignClient;
import com.cyancoder.tax_pay_sys_service.modules.home.dto.res.TestEncryptResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.home.dto.res.TestResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.home.service.ExternalFeignClientAsync;
import com.cyancoder.tax_pay_sys_service.modules.home.service.ExternalService;
import com.cyancoder.tax_pay_sys_service.modules.home.service.TicketService;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.service.ServerInformationService;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.factor.Factor;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.Header;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.auth.AuthRequestDataModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.server_info.ServerInfoResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.repository.FactorRepository;
import com.cyancoder.tax_pay_sys_service.util.CryptoUtils;
import com.cyancoder.tax_pay_sys_service.util.Encryption;
import com.cyancoder.tax_pay_sys_service.util.KeyUtil;
import com.cyancoder.tax_pay_sys_service.util.SignText;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    private final ExternalFeignClientAsync externalFeignClientAsync;

    private final ExternalService externalService;

    private final TicketService ticketService;

    private final ExternalFeignClient externalFeignClient;

    private final ServerInformationService serverInformationService;

    private final FactorRepository factorRepository;

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
        RequestModel body = new RequestModel(header, "GET_TOKEN", data, SellerUser.cyan);

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
        RequestModel body = new RequestModel(header, "GET_TOKEN", data,SellerUser.cyan);

        String normalJsonStr = CryptoUtils.normalJson(body.getPacket(), header);
        String ex = SignText.getSignedText(normalJsonStr, "SHA256WITHRSA", KeyUtil.getPrivateKey(SellerUser.cyan));





        SecretKey aesKey = Encryption.getAESKey(256);
        byte[] iv = Encryption.getRandomNonce(128);

        byte[] encrypt = Encryption.encrypt(iv,aesKey,iv);

        byte[] xor = Encryption.xor(encrypt,iv);




        ServerInfoResponseModel response = serverInformationService.getServerInformation();
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
    public Object getFactors() throws ParseException {


        String date_string1 = "2023-05-01";
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
//        formatter1.setTimeZone(TimeZone.getTimeZone("teh"));
        Date date1 = formatter1.parse(date_string1);

        String date_string2 = "2023-12-20";
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
        Date date2 = formatter2.parse(date_string2);


        return factorRepository.findByCreatedOnBetween(date1,date2).stream()
                .filter(i->i.getId()>276225)
                .filter(i->i.getStatus().toString()!= "removed")
                .toList();
//        return factorRepository.findAll(Pageable.ofSize(12)
////                .getSortOr(Sort.by(Sort.Direction.DESC, "id"))
//        );

    }

}


