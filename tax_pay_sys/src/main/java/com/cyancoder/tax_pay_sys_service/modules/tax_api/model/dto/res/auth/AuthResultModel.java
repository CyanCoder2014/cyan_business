package com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.auth;

import com.cyancoder.tax_pay_sys_service.modules.tax_api.model.dto.res.ResultModel;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat
public class AuthResultModel extends ResultModel {

    public AuthResponsePacketModel result;

}
