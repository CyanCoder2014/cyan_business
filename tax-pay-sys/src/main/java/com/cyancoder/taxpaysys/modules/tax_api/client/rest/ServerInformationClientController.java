package com.cyancoder.taxpaysys.modules.tax_api.client.rest;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info.ServerInfoResultModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "https://tp.tax.gov.ir/req/api/self-tsp/sync", name = "invoice")
//@FeignClient(url = "https://sandboxrc.tax.gov.ir/req/api/self-tsp/sync", name = "invoice")
public interface ServerInformationClientController {


    @PostMapping(value = "/GET_SERVER_INFORMATION", consumes = "application/json",produces = "application/json")
    ServerInfoResultModel getServerInformation(@RequestHeader("Content-Type") String contentType,
                                               @RequestHeader("requestTraceId") String requestTraceId,
                                               @RequestHeader("timestamp") String timestamp,
                                               @RequestBody RequestModel body);



}
