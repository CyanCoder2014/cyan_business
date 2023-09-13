package main.java.com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.inquiry;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.ResponseModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class InquiryResponseModel extends ResponseModel {


    public InquiryResultModel successResponse;

    public InquiryResponseModel(InquiryResultModel result){
        super(result);
        this.successResponse = result;
    }


    public InquiryResponseModel(int errorCode, String errormessage){
        super(errorCode, errormessage);
    }

}
