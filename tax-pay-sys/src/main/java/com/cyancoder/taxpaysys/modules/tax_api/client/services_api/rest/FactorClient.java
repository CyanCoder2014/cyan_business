package com.cyancoder.taxpaysys.modules.tax_api.client.services_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.model.FactorModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestFactorModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "factor-service")////////// need to consider :lb
public interface FactorClient {


    @GetMapping(value = "/v2/api/factor-service/factors")
    List<FactorModel> getItems(@RequestHeader("Authorization") String token,
                               @RequestBody RequestFactorModel requestFactorModel);
}
