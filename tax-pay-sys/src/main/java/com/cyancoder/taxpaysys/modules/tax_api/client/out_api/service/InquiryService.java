package com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service;

import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.rest.InquiryClientController;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.service.CompanyClientService;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.CompanyModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid.InquiryByUidRequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data.InquiryByReferenceNumberDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data.InquiryByTimeRangeDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid.InquiryByUidDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data.InquiryDataByTimeModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid.RequestUidItemModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry.InquiryResponseModel;
import com.cyancoder.taxpaysys.util.KeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

    private final InquiryClientController inquiryClientController;
    private final CompanyClientService companyClientService;

    private final AuthService authService;


    private Header defineHeader(String uniqueCode, String privateKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        Random rnd = new Random();
        Header header = new Header("2023-cyanbusiness-inq-2320011"+ rnd.nextInt(10));
        if (header.getString("Authorization") == "")
            authService.setTokenInHeader(header,  uniqueCode, privateKey);
        return header;
    }

    public InquiryResponseModel getInquiryByUid(String uniqueCode, String companyId, String uid) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        CompanyModel companyModel = companyClientService.getCompany(companyId);
        Header header = defineHeader(uniqueCode, companyModel.getPk());
        InquiryByUidDataModel data = InquiryByUidDataModel.builder().build();
        RequestUidItemModel uid1 = RequestUidItemModel.builder()
                .uid(uid) // for test
                .fiscalId(uniqueCode)
                .build();
        data.add(uid1);
        InquiryByUidRequestModel body = new InquiryByUidRequestModel(header, "INQUIRY_BY_UID", data, companyModel.getPk());

        try {
            return new InquiryResponseModel(inquiryClientController.getInquiryByUid(
                    header.getContentType(),
                    header.getString("requestTraceId"),
                    header.getString("timestamp"),
                    "Bearer "+header.getString("Authorization"),
                    body));
        } catch (Exception e) {
            return new InquiryResponseModel(0, e.getMessage());
        }
    }


    public InquiryResponseModel getInquiryByReferenceNumber(String uniqueCode, String companyId, String reference) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        CompanyModel companyModel = companyClientService.getCompany(companyId);
        Header header = defineHeader(uniqueCode, companyModel.getPk());
        InquiryByReferenceNumberDataModel data = InquiryByReferenceNumberDataModel.builder().build();
        data.setReferenceNumber(new ArrayList<>());
        data.getReferenceNumber().add(reference);  // for test
        RequestModel body = new RequestModel(header, "INQUIRY_BY_REFERENCE_NUMBER", data, companyModel.getPk());

        try {
            return new InquiryResponseModel(inquiryClientController.getInquiryByReferenceNumber(
                    header.getContentType(),
                    header.getString("requestTraceId"),
                    header.getString("timestamp"),
                    "Bearer "+header.getString("Authorization"),
                    body));
        } catch (Exception e) {
            return new InquiryResponseModel(0, e.getMessage());
        }
    }


//    public InquiryResponseModel getInquiryByTime(int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
//
//        SellerUser sellerEnm = SellerUser.getSellerById(seller);
//        Header header = defineHeader(sellerEnm);        InquiryDataByTimeModel data = InquiryDataByTimeModel.builder().time("14020319").build();
//        RequestModel body = new RequestModel(header, "INQUIRY_BY_TIME", data,SellerUser.cyan);
//
//        try {
//            return new InquiryResponseModel(inquiryClientController.getInquiryByTime(
//                    header.getContentType(),
//                    header.getString("requestTraceId"),
//                    header.getString("timestamp"),
//                    "Bearer "+header.getString("Authorization"),
//                    body));
//        } catch (Exception e) {
//            return new InquiryResponseModel(0, e.getMessage());
//        }
//    }
//
//
//    public InquiryResponseModel getInquiryByTimeRange(int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
//
//        SellerUser sellerEnm = SellerUser.getSellerById(seller);
//        Header header = defineHeader(sellerEnm);        InquiryByTimeRangeDataModel data = InquiryByTimeRangeDataModel.builder().startDate("14020119").endDate("14020319").build();
//        RequestModel body = new RequestModel(header, "INQUIRY_BY_TIME_RANGE", data,SellerUser.cyan);
//
//        log.info("header: {}",header);
//        log.info("data: {}",data);
//        log.info("body: {}",body);
//        try {
//            return new InquiryResponseModel(inquiryClientController.getInquiryByTimeRange(
//                    header.getContentType(),
//                    header.getString("requestTraceId"),
//                    header.getString("timestamp"),
//                    "Bearer "+header.getString("Authorization"),
//                    body));
//        } catch (Exception e) {
//            return new InquiryResponseModel(0, e.getMessage());
//        }
//    }



}
