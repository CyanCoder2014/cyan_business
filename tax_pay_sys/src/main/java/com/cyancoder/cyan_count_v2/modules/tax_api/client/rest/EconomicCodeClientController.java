package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.client.rest;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.eco_code.EconomicCodeResultModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "https://tp.tax.gov.ir/req/api/self-tsp/sync", name = "invoice")
//@FeignClient(url = "https://sandboxrc.tax.gov.ir/req/api/self-tsp/sync", name = "invoice")
public interface EconomicCodeClientController {


    @PostMapping(value = "/GET_ECONOMIC_CODE_INFORMATION", consumes = "application/json",produces = "application/json")
    EconomicCodeResultModel getEconomicCode(@RequestHeader("Content-Type") String contentType,
                                            @RequestHeader("requestTraceId") String requestTraceId,
                                            @RequestHeader("timestamp") String timestamp,
                                            @RequestBody RequestModel body);



}
