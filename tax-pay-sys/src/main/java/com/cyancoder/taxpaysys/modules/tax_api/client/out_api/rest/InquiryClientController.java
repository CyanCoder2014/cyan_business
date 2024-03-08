package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid.InquiryByUidRequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry.InquiryResultModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "https://tp.tax.gov.ir/req/api/self-tsp/sync", name = "invoice")
//@FeignClient(url = "https://sandboxrc.tax.gov.ir/req/api/self-tsp/sync", name = "invoice")
public interface InquiryClientController {


    @PostMapping(value = "/INQUIRY_BY_UID", consumes = "application/json",produces = "application/json")
    InquiryResultModel getInquiryByUid(@RequestHeader("Content-Type") String contentType,
                                       @RequestHeader("requestTraceId") String requestTraceId,
                                       @RequestHeader("timestamp") String timestamp,
                                       @RequestHeader("Authorization") String token,
                                       @RequestBody InquiryByUidRequestModel body);

    @PostMapping(value = "/INQUIRY_BY_REFERENCE_NUMBER", consumes = "application/json",produces = "application/json")
    Object getInquiryByReferenceNumber(@RequestHeader("Content-Type") String contentType,
                                    @RequestHeader("requestTraceId") String requestTraceId,
                                    @RequestHeader("timestamp") String timestamp,
                                    @RequestHeader("Authorization") String token,
                                    @RequestBody RequestModel body);


    @PostMapping(value = "/INQUIRY_BY_TIME", consumes = "application/json",produces = "application/json")
    InquiryResultModel getInquiryByTime(@RequestHeader("Content-Type") String contentType,
                                    @RequestHeader("requestTraceId") String requestTraceId,
                                    @RequestHeader("timestamp") String timestamp,
                                    @RequestHeader("Authorization") String token,
                                    @RequestBody RequestModel body);


    @PostMapping(value = "/INQUIRY_BY_TIME_RANGE", consumes = "application/json",produces = "application/json")
    InquiryResultModel getInquiryByTimeRange(@RequestHeader("Content-Type") String contentType,
                                    @RequestHeader("requestTraceId") String requestTraceId,
                                    @RequestHeader("timestamp") String timestamp,
                                    @RequestHeader("Authorization") String token,
                                    @RequestBody RequestModel body);



}
