package com.cyancoder.taxpaysys.modules.tax_api.client.services_api;


import com.cyancoder.taxpaysys.modules.tax_api.model.CompanyModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestCompanyModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "factor-service")////////// need to consider :lb
public interface CompanyClient {


//    @GetMapping(value = "/v2/api/client-service/companies/")
//    public Object getItems();
////    public List<AbstractReadWriteAccess.Item> getItems(@RequestParam Long userId);

    @GetMapping(value = "/v2/api/client-service/companies/")
    CompanyModel getItem(@RequestHeader("Authorization") String token,
                         @RequestBody RequestCompanyModel body);
}