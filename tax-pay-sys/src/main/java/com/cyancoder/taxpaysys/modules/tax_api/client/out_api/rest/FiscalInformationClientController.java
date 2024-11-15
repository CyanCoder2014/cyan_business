package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResultModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "https://tp.tax.gov.ir/req/api/self-tsp/sync", name = "invoice")
//@FeignClient(url = "https://sandboxrc.tax.gov.ir/req/api/self-tsp/sync", name = "invoice")
public interface FiscalInformationClientController {


    @PostMapping(value = "/GET_FISCAL_INFORMATION", consumes = "application/json",produces = "application/json")
    ResultModel getFiscalInformation(@RequestHeader("Content-Type") String contentType,
                                     @RequestHeader("requestTraceId") String requestTraceId,
                                     @RequestHeader("timestamp") String timestamp,
                                     @RequestHeader("Authorization") String token,
                                     @RequestBody RequestModel body);



}
