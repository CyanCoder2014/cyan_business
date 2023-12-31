package com.cyancoder.taxpaysys.modules.tax_api.client.services_api.rest;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "factor-service",url = "lb://factor-service/v2/api/factor-service")////////// need to consider :lb
public interface FactorClient {


    @GetMapping(value = "/factors")
    public Object getItems();
//    public List<AbstractReadWriteAccess.Item> getItems(@RequestParam Long userId);
}
