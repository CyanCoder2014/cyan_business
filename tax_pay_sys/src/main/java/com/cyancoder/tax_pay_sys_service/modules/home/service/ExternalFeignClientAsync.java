package com.cyancoder.tax_pay_sys_service.modules.home.service;

import com.cyancoder.tax_pay_sys_service.modules.home.client.ExternalFeignClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@Service
@AllArgsConstructor
@Slf4j
public class ExternalFeignClientAsync {

    private ExternalFeignClient externalFeignClient;

    @Async
    public CompletableFuture<String> getRecordsAsync() throws ExecutionException, InterruptedException {
        CompletableFuture ex = CompletableFuture.completedFuture(externalFeignClient.getRecords());
//        ex.thenApply(s->"sdsd");
//        Thread.sleep(1000);

        log.info("async service test with ex {}", ex.get());

        return ex;
    }




}