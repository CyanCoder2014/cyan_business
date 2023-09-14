package com.cyancoder.tax_pay_sys_service.modules.tax_api.rest;


import com.cyancoder.tax_pay_sys_service.modules.content.api.DefaultTaxApiClient;
import com.cyancoder.tax_pay_sys_service.modules.content.api.TaxApi;
import com.cyancoder.tax_pay_sys_service.modules.content.dto.*;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.auth.Token;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.rest.InvoiceTaxClientController;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.service.AuthService;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.service.InvoiceService;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.service.ServerInformationService;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.Header;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.invoice.BodyItems;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.invoice.HeaderItems;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.invoice.InvoiceRequestDataModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.invoice.InvoiceRequestModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.server_info.ServerInfoPubKeyModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.server_info.ServerInfoResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.service.FactorService;
import com.cyancoder.tax_pay_sys_service.modules.transfer.api.ObjectTransferApiImpl;
import com.cyancoder.tax_pay_sys_service.modules.transfer.api.TransferApi;
import com.cyancoder.tax_pay_sys_service.modules.transfer.config.ApiConfig;
import com.cyancoder.tax_pay_sys_service.modules.transfer.dto.AsyncResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.transfer.impl.encrypter.DefaultEncrypter;
import com.cyancoder.tax_pay_sys_service.modules.transfer.impl.signatory.InMemorySignatory;
import com.cyancoder.tax_pay_sys_service.util.KeyUtil;
import com.cyancoder.tax_pay_sys_service.util.TaxUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/v2/api/invoice")
@Slf4j
public class InvoiceController {



//    private TaxApi taxApi;




    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private InvoiceTaxClientController invoiceTaxClientController;
//    @Autowired
//    private  ServerInformationService serverInformationService;
//    @Autowired
//    private AuthService authService;


    @Autowired
    private FactorService factorService;



    @GetMapping("send-invoice")
    List<Object> submitInvoice(HttpServletRequest request) throws Exception {

        return invoiceService.sendInvoice();
    }


    @PostMapping("send-invoice-sync")
    Object submitInvoiceSync(@RequestParam String basedOn,
                             @RequestParam String codeFrom,
                             @RequestParam String codeTo,
                             @RequestParam String fromDate,
                             @RequestParam String toDate,
                             @RequestParam int seller
                             ) throws Exception {



            return factorService.getFactorsToSubmit(basedOn, codeFrom, codeTo,
                                                    fromDate,toDate,seller); // for test

//        return invoiceTaxClientController.sendInvoiceNormal(
//                header.getString("Content-Type"),
//                header.getString("requestTraceId"),
//                header.getString("timestamp"),
//                header.getString("Authorization"),
//                bodyHttp
//        );
    }



    @PostMapping("invoice-correction")
    Object invoiceCorrection(@RequestParam Long factorId,
                             @RequestParam int seller
    ) throws Exception {

        return factorService.factorCorrection(factorId,seller);
    }

    @PostMapping("invoice-cancellation")
    Object invoiceCancellation(@RequestParam Long factorId,
                             @RequestParam int seller
    ) throws Exception {

        return factorService.factorCancellation(factorId,seller);
    }


}
