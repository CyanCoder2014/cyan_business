package com.cyancoder.taxpaysys.modules.tax_api.rest;


import com.cyancoder.taxpaysys.modules.tax_api.entity.FactorTaxEntity;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry.InquiryResponseModel;
import com.cyancoder.taxpaysys.modules.tax_api.service.InquiryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@RestController
@RequestMapping("/v2/api/tax-service/inquiry")
@Slf4j
public class InquiryController {

    @Autowired
    InquiryService inquiryService;


    @GetMapping("/get-by-uid")
    Object getInquiryByUid(@RequestHeader("UniqueCode")String uniqueCode,
                           @RequestParam String companyId,
                           @RequestParam String uid) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        return inquiryService.getInquiryByUid(uniqueCode, companyId, uid);
//        InquiryResponseModel response = inquiryService.getInquiryByUid(uniqueCode, companyId, uid);
//        return response.successResponse != null ? response.successResponse.get(0)  : response;
    }

    @GetMapping("/get-by-reference")
    Object getInquiryByReferenceNumber(@RequestHeader("UniqueCode")String uniqueCode,
                                       @RequestParam String companyId,
                                       @RequestParam String reference
                                       ) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        return inquiryService.getInquiryByReferenceNumber(uniqueCode, companyId, reference);
//        return response.successResponse != null ? response.successResponse.result.data  : response;
    }

//    @GetMapping("/get-by-time")
//    Object getInquiryByTime(HttpServletRequest request, @RequestParam int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
//
//        ResponseModel response = inquiryService.getInquiryByTime(seller);
//        return response.successResponse != null ? response.successResponse.result.data  : response;
//    }
//
//    @GetMapping("/get-by-time-range")
//    Object getInquiryByTimeRange(HttpServletRequest request, @RequestParam int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
//
//        ResponseModel response = inquiryService.getInquiryByTimeRange(seller);
//        return response.successResponse != null ? response.successResponse.result.data  : response;
//    }



    @GetMapping("/get-references")
    List<FactorTaxEntity> getReferences(@RequestHeader("UniqueCode")String uniqueCode,
                                        @RequestParam String companyId,
                                        @RequestParam String factorId
    )  {

        return inquiryService.getReferences(factorId);
    }


}
