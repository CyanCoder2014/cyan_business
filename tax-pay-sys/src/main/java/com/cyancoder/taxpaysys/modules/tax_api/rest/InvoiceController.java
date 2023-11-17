package com.cyancoder.taxpaysys.modules.tax_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.client.rest.InvoiceTaxClientController;
import com.cyancoder.taxpaysys.modules.tax_api.client.service.InvoiceService;
import com.cyancoder.taxpaysys.modules.tax_api.service.FactorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/tax/invoice")
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
