package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.client.service;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.client.rest.InquiryClientController;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.Header;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.RequestModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid.InquiryByUidRequestModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data.InquiryByReferenceNumberDataModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data.InquiryByTimeRangeDataModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid.InquiryByUidDataModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data.InquiryDataByTimeModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.req.inquiry.inquiry_data.inquiry_by_uid.RequestUidItemModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.ResponseModel;
import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.inquiry.InquiryResponseModel;
import com.cyancoder.tax_pay_sys_service.util.KeyUtil;
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

    private final AuthService authService;


    private Header defineHeader(SellerUser seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        Random rnd = new Random();
        Header header = new Header("2023-cyanbusiness-inq-2320011"+ rnd.nextInt(10));
        if (header.getString("Authorization") == "")
            authService.setTokenInHeader(header, seller);
        return header;
    }

    public InquiryResponseModel getInquiryByUid(String uid, int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        Header header = defineHeader(sellerEnm);
        InquiryByUidDataModel data = InquiryByUidDataModel.builder().build();
        RequestUidItemModel uid1 = RequestUidItemModel.builder()
                .uid(uid) // for test
                .fiscalId(KeyUtil.getClientId(sellerEnm))
                .build();
        data.add(uid1);
        InquiryByUidRequestModel body = new InquiryByUidRequestModel(header, "INQUIRY_BY_UID", data, sellerEnm);

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


    public InquiryResponseModel getInquiryByReferenceNumber(String reference, int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        Header header = defineHeader(sellerEnm);        InquiryByReferenceNumberDataModel data = InquiryByReferenceNumberDataModel.builder().build();
        data.setReferenceNumber(new ArrayList<>());
        data.getReferenceNumber().add(reference);  // for test
        RequestModel body = new RequestModel(header, "INQUIRY_BY_REFERENCE_NUMBER", data,SellerUser.cyan);

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


    public InquiryResponseModel getInquiryByTime(int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        Header header = defineHeader(sellerEnm);        InquiryDataByTimeModel data = InquiryDataByTimeModel.builder().time("14020319").build();
        RequestModel body = new RequestModel(header, "INQUIRY_BY_TIME", data,SellerUser.cyan);

        try {
            return new InquiryResponseModel(inquiryClientController.getInquiryByTime(
                    header.getContentType(),
                    header.getString("requestTraceId"),
                    header.getString("timestamp"),
                    "Bearer "+header.getString("Authorization"),
                    body));
        } catch (Exception e) {
            return new InquiryResponseModel(0, e.getMessage());
        }
    }


    public InquiryResponseModel getInquiryByTimeRange(int seller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {

        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        Header header = defineHeader(sellerEnm);        InquiryByTimeRangeDataModel data = InquiryByTimeRangeDataModel.builder().startDate("14020119").endDate("14020319").build();
        RequestModel body = new RequestModel(header, "INQUIRY_BY_TIME_RANGE", data,SellerUser.cyan);

        log.info("header: {}",header);
        log.info("data: {}",data);
        log.info("body: {}",body);
        try {
            return new InquiryResponseModel(inquiryClientController.getInquiryByTimeRange(
                    header.getContentType(),
                    header.getString("requestTraceId"),
                    header.getString("timestamp"),
                    "Bearer "+header.getString("Authorization"),
                    body));
        } catch (Exception e) {
            return new InquiryResponseModel(0, e.getMessage());
        }
    }



}
