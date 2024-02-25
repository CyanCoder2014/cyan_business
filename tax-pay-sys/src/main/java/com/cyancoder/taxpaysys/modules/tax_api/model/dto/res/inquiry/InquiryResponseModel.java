package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResponseModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

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
