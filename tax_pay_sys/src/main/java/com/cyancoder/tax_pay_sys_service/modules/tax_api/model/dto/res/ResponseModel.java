package com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@JsonFormat
public class ResponseModel{


    public ResultModel successResponse;
    public int errorCode = 0;
    public String errormessage;

    public ResponseModel(ResultModel result){
        this.successResponse = result;
    }


    public ResponseModel(int errorCode,String errormessage){
//        if(errorCode != 0)
//            this.errorCode = errorCode;
//        else
//            this.errorCode = errormessage.length() > 3 ?  Integer.valueOf(errormessage.substring(1,4)) : 500;

        this.errormessage = errormessage;
    }
}
