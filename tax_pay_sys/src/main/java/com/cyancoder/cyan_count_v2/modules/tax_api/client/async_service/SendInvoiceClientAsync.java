package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.client.async_service;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.rest.InvoiceTaxClientController;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.invoice.InvoiceRequestModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.invoice.SendInvoiceResponseModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
@AllArgsConstructor
@Slf4j
public class SendInvoiceClientAsync {

    private InvoiceTaxClientController invoiceTaxClientController;

    @Async
    public CompletableFuture<SendInvoiceResponseModel> sendInvoiceAsync(String contentType,
                                                                        String requestTraceId,
                                                                        String timestamp,
                                                                        String token,
                                                                        InvoiceRequestModel body) throws ExecutionException, InterruptedException {
        log.info("async called");

        CompletableFuture asyncResult = null;
        try {
            asyncResult = CompletableFuture.completedFuture(invoiceTaxClientController.sendInvoiceNormal(
                    contentType,
                    requestTraceId,
                    timestamp,
                    token,
                    body
            ));
            log.info("result of call send invoice api: {}", asyncResult.get());
        } catch (Exception e) {
            log.error("result of call send invoice api acure error: {}", e.getMessage());
        }

        return asyncResult;
    }



}