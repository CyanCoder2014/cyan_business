package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.auth;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResponseModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class AuthResponseModel extends ResponseModel {


    public AuthResultModel successResponse;

    public AuthResponseModel(AuthResultModel result){
        super(result);
        this.successResponse = result;
    }


    public AuthResponseModel(int errorCode,String errormessage){
        super(errorCode, errormessage);
    }

}
