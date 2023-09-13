package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.rest;


import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.service.FiscalInformationService;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.service.InquiryService;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.ResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.inquiry.InquiryResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/v2/api/inquiry")
@Slf4j
public class InquiryController {

    @Autowired
    InquiryService inquiryService;


    @GetMapping("/get-by-uid")
    Object getInquiryByUid(@RequestParam String uid, @RequestParam int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        InquiryResponseModel response = inquiryService.getInquiryByUid(uid, seller);
        return response.successResponse != null ? response.successResponse.result.data  : response;
    }

    @GetMapping("/get-by-reference")
    Object getInquiryByReferenceNumber(@RequestParam String reference, @RequestParam int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        InquiryResponseModel response = inquiryService.getInquiryByReferenceNumber(reference, seller);
        return response.successResponse != null ? response.successResponse.result.data  : response;
    }

    @GetMapping("/get-by-time")
    Object getInquiryByTime(HttpServletRequest request, @RequestParam int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        ResponseModel response = inquiryService.getInquiryByTime(seller);
        return response.successResponse != null ? response.successResponse.result.data  : response;
    }

    @GetMapping("/get-by-time-range")
    Object getInquiryByTimeRange(HttpServletRequest request, @RequestParam int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        ResponseModel response = inquiryService.getInquiryByTimeRange(seller);
        return response.successResponse != null ? response.successResponse.result.data  : response;
    }
}
