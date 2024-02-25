package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.inquiry;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResponseModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class InquiryResponseModel  {


    public InquiryResultModel successResponse;

    public int errorCode = 0;
    public String errormessage;

    public InquiryResponseModel(InquiryResultModel result){
//        super(result);
        this.successResponse = result;
    }


    public InquiryResponseModel(int errorCode, String errormessage){

        this.errormessage = errormessage;;
    }




}
