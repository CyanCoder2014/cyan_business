package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.eco_code;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResponseModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class EconomicCodeResponseModel extends ResponseModel {


    public EconomicCodeResultModel successResponse;

    public EconomicCodeResponseModel(EconomicCodeResultModel result){
        super(result);
        this.successResponse = result;
    }


    public EconomicCodeResponseModel(int errorCode, String errormessage){
        super(errorCode, errormessage);
    }

}
