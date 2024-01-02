package com.cyancoder.taxpaysys.modules.tax_api.client.services_api.rest;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "factor-service")////////// need to consider :lb
public interface FactorClient {


    @GetMapping(value = "/v2/api/factor-service/factors")
    public Object getItems();
//    public List<AbstractReadWriteAccess.Item> getItems(@RequestParam Long userId);

    @GetMapping(value = "/v2/api/factor-service/factors")
    public Object getItemsT(@RequestHeader("Authorization") String token);
//    public List<AbstractReadWriteAccess.Item> getItems(@RequestParam Long userId);
}
