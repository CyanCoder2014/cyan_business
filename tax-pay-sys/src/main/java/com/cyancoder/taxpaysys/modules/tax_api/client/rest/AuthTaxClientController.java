package com.cyancoder.taxpaysys.modules.tax_api.client.rest;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.auth.AuthResultModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "https://tp.tax.gov.ir/req/api/self-tsp/sync", name = "auth")
//@FeignClient(url = "https://sandboxrc.tax.gov.ir/req/api/self-tsp/sync", name = "auth")
public interface AuthTaxClientController {

    @PostMapping(value = "/GET_TOKEN", consumes = "application/json", produces = "application/json")
    AuthResultModel getToken(@RequestHeader("Content-Type") String contentType,
                             @RequestHeader("requestTraceId") String requestTraceId,
                             @RequestHeader("timestamp") String timestamp,

                             @RequestBody RequestModel authRequestModel);




}



