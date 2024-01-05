package com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info;

import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.ResponseModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class ServerInfoResponseModel extends ResponseModel {


    public ServerInfoResultModel successResponse;

    public ServerInfoResponseModel(ServerInfoResultModel result){
        super(result);
        this.successResponse = result;
    }


    public ServerInfoResponseModel(int errorCode,String errormessage){
        super(errorCode, errormessage);
    }

}
