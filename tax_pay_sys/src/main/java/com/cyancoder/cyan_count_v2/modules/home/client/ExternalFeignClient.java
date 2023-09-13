package main.java.com.cyancoder.tax_pay_sys_service.modules.home.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient(url = "http://localhost:8081", name = "external")
public interface ExternalFeignClient {

    @GetMapping(value = "/", produces = "application/json")
    String getRecords();


}
