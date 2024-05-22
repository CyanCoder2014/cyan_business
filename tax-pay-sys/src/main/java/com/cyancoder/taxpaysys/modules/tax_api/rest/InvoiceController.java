package com.cyancoder.taxpaysys.modules.tax_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest.InvoiceTaxClientController;
import com.cyancoder.taxpaysys.modules.tax_api.entity.FactorTaxEntity;
import com.cyancoder.taxpaysys.modules.tax_api.service.FactorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/api/tax-service/invoice")
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
    List<FactorTaxEntity> submitInvoiceSync(@RequestHeader("UniqueCode") String uniqueCode,
                             @RequestParam String basedOn,
                             @RequestParam String codeFrom,
                             @RequestParam String codeTo,
                             @RequestParam String fromDate,
                             @RequestParam String toDate,
                             @RequestParam String factorId,
                             @RequestParam String companyId
    ) throws Exception {

        List<FactorTaxEntity> res = factorService.getFactorsToSubmit(uniqueCode, basedOn, codeFrom, codeTo,
                fromDate, toDate, factorId, companyId);


        if (res == null)
            throw new Exception("فاکتوری جهت ارسال یافت نشد");

        return res; // for test

//        return invoiceTaxClientController.sendInvoiceNormal(
//                header.getString("Content-Type"),
//                header.getString("requestTraceId"),
//                header.getString("timestamp"),
//                header.getString("Authorization"),
//                bodyHttp
//        );
    }


    @PostMapping("invoice-correction")
    Object invoiceCorrection(@RequestHeader("UniqueCode") String uniqueCode,
                             @RequestParam String factorId,
                             @RequestParam String companyId
    ) throws Exception {

        return factorService.factorCorrection(uniqueCode, "", "", "",
                "", "", factorId, companyId);
    }


    @PostMapping("invoice-correction-list")
    Object invoiceCorrectionList(@RequestHeader("UniqueCode") String uniqueCode,
                             @RequestParam String basedOn,
                             @RequestParam String codeFrom,
                             @RequestParam String codeTo,
                             @RequestParam String fromDate,
                             @RequestParam String toDate,
                             @RequestParam String factorId,
                             @RequestParam String companyId
    ) throws Exception {

        return factorService.factorCorrection(uniqueCode, basedOn, codeFrom, codeTo,
                fromDate, toDate, factorId, companyId);
    }




    @PostMapping("invoice-cancellation")
    Object invoiceCancellation(@RequestHeader("UniqueCode") String uniqueCode,
                             @RequestParam String factorId,
                             @RequestParam String companyId
    ) throws Exception {

        return factorService.factorCancellation(uniqueCode, null, null, null,
                null, null, factorId, companyId);
    }


    @PostMapping("invoice-cancellation-list")
    Object invoiceCancellationList(@RequestHeader("UniqueCode") String uniqueCode,
                             @RequestParam String basedOn,
                             @RequestParam String codeFrom,
                             @RequestParam String codeTo,
                             @RequestParam String fromDate,
                             @RequestParam String toDate,
                             @RequestParam String factorId,
                             @RequestParam String companyId
    ) throws Exception {

        return factorService.factorCancellation(uniqueCode, basedOn, codeFrom, codeTo,
                fromDate, toDate, factorId, companyId);
    }

}
