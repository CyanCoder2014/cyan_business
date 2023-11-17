package com.cyancoder.tax_pay_sys_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableFeignClients
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@EnableEurekaClient
public class TaxPaySysServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaxPaySysServiceApplication.class, args);
	}

}

