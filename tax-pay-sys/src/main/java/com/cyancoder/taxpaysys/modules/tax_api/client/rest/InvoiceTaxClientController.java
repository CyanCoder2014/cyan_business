package com.cyancoder.taxpaysys.modules.tax_api.client.rest;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice.InvoiceRequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.invoice.SendInvoiceResponseModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "https://tp.tax.gov.ir/req/api/self-tsp/async", name = "invoice")
//@FeignClient(url = "https://sandboxrc.tax.gov.ir/req/api/self-tsp/async", name = "invoice")
public interface InvoiceTaxClientController {

    @PostMapping(value = "/normal-enqueue", consumes = "application/json",produces = "application/json")
    SendInvoiceResponseModel sendInvoiceNormal(@RequestHeader("Content-Type") String contentType,
                                               @RequestHeader("requestTraceId") String requestTraceId,
                                               @RequestHeader("timestamp") String timestamp,
                                               @RequestHeader("Authorization") String token,
                                               @RequestBody InvoiceRequestModel body);


    @PostMapping(value = "/fast-enqueue", produces = "application/json")
    SendInvoiceResponseModel sendInvoiceFast();


}
