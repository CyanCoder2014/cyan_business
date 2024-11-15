package com.cyancoder.taxpaysys.modules.tax_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest.InvoiceTaxClientController;
import com.cyancoder.taxpaysys.modules.tax_api.service.FactorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/tax/invoice")
@Slf4j
public class InvoiceController {



//    private TaxApi taxApi;



//
//    @Autowired
//    private InvoiceService invoiceService;
    @Autowired
    private InvoiceTaxClientController invoiceTaxClientController;
//    @Autowired
//    private  ServerInformationService serverInformationService;
//    @Autowired
//    private AuthService authService;


    @Autowired
    private FactorService factorService;



//    @GetMapping("send-invoice-async")
//    List<Object> submitInvoice(HttpServletRequest request) throws Exception {
//
//        return invoiceService.sendInvoice();
//    }


    @PostMapping("send-invoice")
    Object submitInvoiceSync(@RequestHeader("UniqueCode")String uniqueCode,
                             @RequestParam String basedOn,
                             @RequestParam String codeFrom,
                             @RequestParam String codeTo,
                             @RequestParam String fromDate,
                             @RequestParam String toDate,
                             @RequestParam String companyId
                             ) throws Exception {



            return factorService.getFactorsToSubmit(uniqueCode, basedOn, codeFrom, codeTo,
                                                    fromDate,toDate,companyId); // for test

//        return invoiceTaxClientController.sendInvoiceNormal(
//                header.getString("Content-Type"),
//                header.getString("requestTraceId"),
//                header.getString("timestamp"),
//                header.getString("Authorization"),
//                bodyHttp
//        );
    }



//    @PostMapping("invoice-correction")
//    Object invoiceCorrection(@RequestHeader("UniqueCode")String uniqueCode,
//                             @RequestParam Long factorId,
//                             @RequestParam String companyId
//    ) throws Exception {
//
//        return factorService.factorCorrection(uniqueCode, factorId,companyId);
//    }
//
//    @PostMapping("invoice-cancellation")
//    Object invoiceCancellation(@RequestHeader("UniqueCode")String uniqueCode,
//                               @RequestParam Long factorId,
//                             @RequestParam String companyId
//    ) throws Exception {
//
//        return factorService.factorCancellation(uniqueCode, factorId,companyId);
//    }


}
