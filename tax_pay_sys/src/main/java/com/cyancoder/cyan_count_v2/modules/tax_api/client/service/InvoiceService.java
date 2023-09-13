package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.client.service;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.async_service.SendInvoiceClientAsync;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.auth.Token;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.Header;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.auth.AuthRequestDataModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.invoice.InvoiceRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {

    private final SendInvoiceClientAsync sendInvoiceClientAsync;

    private final AuthService authService;

    public List<Object> sendInvoice() throws Exception {

        log.info("service called");

        Token bearerTokenWrapper = Token.getInstance();
        log.info("token from tokenWrapper: {}", bearerTokenWrapper.getToken());

        Random rnd = new Random();
        Header header = new Header("2023-cyanbusiness-inv-2320011"+ rnd.nextInt(10));
        if (header.getString("Authorization") == "")
             authService.setTokenInHeader(header, SellerUser.cyan);


        AuthRequestDataModel data = AuthRequestDataModel.builder().username("A119W2").build();
        InvoiceRequestModel body = new InvoiceRequestModel(header, "INVOICE.V01", data,null,SellerUser.cyan);


        return IntStream.range(0,1).parallel()
                .mapToObj(i->
                {
                    try {
                        return sendInvoiceClientAsync.sendInvoiceAsync(
                                header.getContentType(),
                                header.getString("requestTraceId"),
                                header.getString("timestamp"),
                                "Bearer "+header.getString("Authorization"),
                                body);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(this::mapToProductResponse)
                .toList();
    }


    private Object mapToProductResponse(Object object) {
        return object;
    }



}
